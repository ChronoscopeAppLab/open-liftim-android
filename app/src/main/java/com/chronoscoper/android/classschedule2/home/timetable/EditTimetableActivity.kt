/*
 * Copyright 2018 Chronoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chronoscoper.android.classschedule2.home.timetable

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.DialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.InfoRecyclerViewAdapter
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.InfoRemoteModel
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import com.chronoscoper.android.classschedule2.task.RegisterInfoService
import com.chronoscoper.android.classschedule2.transition.FabExpandTransition
import com.chronoscoper.android.classschedule2.util.EventMessage
import com.chronoscoper.android.classschedule2.util.obtainColorCorrespondsTo
import com.chronoscoper.android.classschedule2.util.progressiveFadeInTransition
import com.chronoscoper.android.classschedule2.view.PopupMenuCompat
import com.chronoscoper.android.classschedule2.view.RecyclerViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.greenrobot.eventbus.EventBus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.parceler.Parcels

class EditTimetableActivity : BaseActivity() {
    companion object {
        private const val TAG = "EditTimetableActivity"
        private const val SOURCE_TIMETABLE = "SOURCE_TIMETABLE"
        fun openWithSourceTimetable(context: Context, source: Info?, options: Bundle? = null) {
            context.startActivity(Intent(context, EditTimetableActivity::class.java)
                    .putExtra(SOURCE_TIMETABLE, Parcels.wrap(source)), options)
        }

        private const val RC_PICK_SUBJECT = 100
        private const val EXTRA_INDEX = "INDEX"
    }

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val liftimCodeImage by bindView<ImageView>(R.id.liftim_code_image)
    private val liftimCodeLabel by bindView<TextView>(R.id.liftim_code)
    private val dateLabel by bindView<TextView>(R.id.date)
    private val info by bindView<EditText>(R.id.info)
    private val classList by bindView<RecyclerView>(R.id.list)
    private val fab by bindView<FloatingActionButton>(R.id.fab)
    private val importWeekly by bindView<View>(R.id.import_weekly)

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_edit_timetable)
        setSupportActionBar(toolbar)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition = FabExpandTransition()
        }

        initialize()
    }

    private var date: DateTime? = null
    private var isManager = false
    private var id: String? = null
    private val compositeDisposable = CompositeDisposable()

    private fun initialize() {
        val liftimCode = LiftimContext.getLiftimCode()
        Glide.with(this)
                .load(LiftimContext.getApiUrl("liftim_code_image.png" +
                        "?liftim_code=$liftimCode" +
                        "&token=${LiftimContext.getToken()}"))
                .apply(RequestOptions.circleCropTransform())
                .transition(progressiveFadeInTransition())
                .into(liftimCodeImage)
        val liftimCodeInfo = LiftimContext.getOrmaDatabase()
                .selectFromLiftimCodeInfo().liftimCodeEq(liftimCode)
                .firstOrNull()
                ?: kotlin.run { finish(); return }
        liftimCodeLabel.text = liftimCodeInfo.name
        isManager = liftimCodeInfo.isManager
        val target = obtainTargetElement()
        id = target?.id
        if (target == null) {
            date = DateTime.now(DateTimeZone.getDefault()).plusDays(1)
        } else {
            date = DateTime.parse(target.date, DateTimeFormat.forPattern("yyyy/MM/dd"))
            if (target.detail != null) {
                info.setText(target.detail)
            }
        }
        dateLabel.text = date!!.toString(DateTimeFormat.fullDate())
        dateLabel.setOnClickListener {
            DatePickerDialog(this,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        date = DateTime(year, month + 1, dayOfMonth, 0, 0)
                        dateLabel.text = date!!.toString(DateTimeFormat.fullDate())
                    }, date!!.year, date!!.monthOfYear - 1, date!!.dayOfMonth).show()
        }
        if (target?.timetable != null) {
            classList.adapter = ClassAdapter(this, LiftimContext.getGson()
                    .fromJson(target.timetable, InfoRemoteModel.Timetable::class.java))
        } else {
            classList.adapter = ClassAdapter(this, null)
        }
        classList.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        fab.setOnClickListener {
            startActivityForResult(Intent(this, SubjectPickerActivity::class.java),
                    RC_PICK_SUBJECT)
        }

        val importPopup = PopupMenuCompat(this, importWeekly)
        importPopup.inflate(R.menu.import_weekly)
        importWeekly.setOnTouchListener(importPopup.dragToOpenListener)
        importPopup.setOnMenuItemClickListener {
            var dayOfWeek = 0
            when (it.itemId) {
                R.id.sunday -> dayOfWeek = 1
                R.id.monday -> dayOfWeek = 2
                R.id.tuesday -> dayOfWeek = 3
                R.id.wednesday -> dayOfWeek = 4
                R.id.thursday -> dayOfWeek = 5
                R.id.friday -> dayOfWeek = 6
                R.id.saturday -> dayOfWeek = 7
            }
            compositeDisposable.add(
                    LiftimContext.getOrmaDatabase()
                            .selectFromWeeklyItem()
                            .liftimCodeEq(LiftimContext.getLiftimCode())
                            .dayOfWeekEq(dayOfWeek)
                            .executeAsObservable()
                            .firstOrError()
                            .map {
                                it.subjects = LiftimContext.getGson()
                                        .fromJson(it.serializedSubjects,
                                                Array<String>::class.java)
                                return@map it
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                (classList.adapter as? ClassAdapter)?.let { adapter ->
                                    adapter.data.clear()
                                    it.subjects.forEachIndexed { index, subject ->
                                        adapter.data.add(index, InfoRemoteModel.SubjectElement()
                                                .apply {
                                                    this.subject = subject
                                                })
                                        adapter.minIndex = it.minIndex
                                        adapter.notifyDataSetChanged()
                                    }
                                }
                            }, {
                                Log.e(TAG, "Timetable for specified day-of-week not found", it)
                            })
            )
            true
        }
        importWeekly.setOnClickListener {
            importPopup.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return
        if (requestCode == RC_PICK_SUBJECT) {
            val index = data.getIntExtra(EXTRA_INDEX, -1)
            val subject = data.getStringExtra(SubjectPickerActivity.EXTRA_SUBJECT)
            val detail = data.getStringExtra(SubjectPickerActivity.EXTRA_DETAIL)
            val adapter = classList.adapter as ClassAdapter
            if (index < 0) {
                adapter.data.add(InfoRemoteModel.SubjectElement().apply {
                    this.subject = subject
                    this.detail = detail
                })
                adapter.notifyItemInserted(adapter.data.size - 1)
            } else {
                try {
                    adapter.data[index].apply {
                        this.subject = subject
                        this.detail = detail
                    }
                    adapter.notifyItemChanged(index)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to alter data on specified position", e)
                }
            }
        }
    }

    private fun obtainTargetElement(): Info? {
        return Parcels.unwrap<Info>(intent.getParcelableExtra(SOURCE_TIMETABLE))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isManager) {
            menuInflater.inflate(R.menu.options_edit_timetable_manager, menu)
        } else {
            menuInflater.inflate(R.menu.options_edit_timetable, menu)
        }
        return true
    }

    private fun createElementFromCurrentState(): Info {
        return Info()
                .apply {
                    liftimCode = LiftimContext.getLiftimCode()
                    id = this@EditTimetableActivity.id
                    title = ""
                    detail = info.text.toString()
                    weight = 1
                    date = this@EditTimetableActivity.date?.toString("yyyy/MM/dd")
                    type = Info.TYPE_TIMETABLE
                    timetable = (classList.adapter as ClassAdapter).generateCurrentStateJson()
                    removable = true
                    addedBy = Info.LOCAL
                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item ?: return true
        when (item.itemId) {
            R.id.options_register_local -> {
                registerLocal(createElementFromCurrentState())
                animateFinish()
                EventBus.getDefault().let {
                    it.post(EventMessage.of(TimetableFragment.EVENT_TIMETABLE_UPDATED))
                    it.post(EventMessage.of(InfoRecyclerViewAdapter.EVENT_ENTRY_UPDATED))
                }
            }
            R.id.options_register_remote -> {
                val element = createElementFromCurrentState()
                registerLocal(element)
                registerRemote(element)
                animateFinish()
                EventBus.getDefault().let {
                    it.post(EventMessage.of(TimetableFragment.EVENT_TIMETABLE_UPDATED))
                    it.post(EventMessage.of(InfoRecyclerViewAdapter.EVENT_ENTRY_UPDATED))
                }
            }
            R.id.options_change_min_indx -> {
                ChangeMinIndexDialog().show(supportFragmentManager, null)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun registerLocal(info: Info) {
        if (info.id == null) {
            info.id = DateTime.now().toString(DateTimeFormat.fullDateTime())
        } else {
            LiftimContext.getOrmaDatabase().deleteFromInfo()
                    .liftimCodeEq(LiftimContext.getLiftimCode())
                    .idEq(info.id)
                    .execute()
        }
        LiftimContext.getOrmaDatabase().insertIntoInfo(info)
    }

    private fun registerRemote(info: Info) {
        val content = InfoRemoteModel.InfoBody()
        content.apply {
            id = info.id
            title = info.title
            detail = info.detail
            weight = info.weight
            date = info.date
            type = info.type
            timetable = LiftimContext.getGson()
                    .fromJson(info.timetable, InfoRemoteModel.Timetable::class.java)
            removable = info.removable
        }
        RegisterInfoService.start(this, LiftimContext.getGson().toJson(content))
    }

    class ClassAdapter(
            private val activity: EditTimetableActivity,
            initialItem: InfoRemoteModel.Timetable?)
        : RecyclerView.Adapter<ClassAdapter.DragViewHolder>() {
        val data = mutableListOf<InfoRemoteModel.SubjectElement>()
        var minIndex = 1
            set (value) {
                field = value
                notifyDataSetChanged()
            }

        init {
            if (initialItem != null) {
                data.addAll(initialItem.subjects)
                minIndex = initialItem.subjectMinIndex
            }
        }

        private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                    recyclerView: RecyclerView?,
                    viewHolder: RecyclerView.ViewHolder?,
                    target: RecyclerView.ViewHolder?): Boolean {
                val from = viewHolder?.adapterPosition ?: return true
                val to = target?.adapterPosition ?: return true
                val temp = data[from]
                data.removeAt(from)
                data.add(to, temp)
                notifyItemChanged(from)
                notifyItemChanged(to)
                notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                // No handle
            }
        })

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        private val inflater by lazy { LayoutInflater.from(activity) }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DragViewHolder =
                DragViewHolder(
                        inflater.inflate(R.layout.timetable_editor_item, parent, false))

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: DragViewHolder, position: Int) {
            val view = holder.itemView
            val item = data[position]

            val index = view.findViewById<TextView>(R.id.index)
            val subject = view.findViewById<TextView>(R.id.subject)
            val detail = view.findViewById<TextView>(R.id.detail)
            val delete = view.findViewById<View>(R.id.delete)

            index.text = (position + minIndex).toString()
            subject.text = item.subject
            index.background.setColorFilter(
                    obtainColorCorrespondsTo(item.subject), PorterDuff.Mode.SRC_IN)
            detail.text = item.detail
            delete.setOnClickListener {
                data.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
            view.setOnClickListener {
                showClassEditorDialog(position, item.subject, item.detail ?: "")
            }
        }

        inner class DragViewHolder(itemView: View) : RecyclerViewHolder(itemView) {
            init {
                itemView.findViewById<View>(R.id.drag_handle)
                        .setOnTouchListener { _, motionEvent ->
                            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                                itemTouchHelper.startDrag(this)
                            }
                            return@setOnTouchListener false
                        }
            }
        }

        fun generateCurrentStateJson(): String {
            return InfoRemoteModel.Timetable().apply {
                subjectMinIndex = minIndex
                subjects = data.toTypedArray()
            }.toString()
        }

        private fun showClassEditorDialog(index: Int, subject: String, detail: String) {
            val intent = Intent(activity, SubjectPickerActivity::class.java)
            intent.apply {
                putExtra(EXTRA_INDEX, index)
                putExtra(SubjectPickerActivity.EXTRA_SUBJECT, subject)
                putExtra(SubjectPickerActivity.EXTRA_DETAIL, detail)
            }
            activity.startActivityForResult(intent, RC_PICK_SUBJECT)
        }
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            fab.show()
        }, 350)
    }

    override fun onPause() {
        super.onPause()
        fab.hide()
    }

    class ChangeMinIndexDialog : DialogFragment() {
        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?): View? =
                inflater.inflate(R.layout.fragment_change_min_index, container, false)

        private val startTime by bindView<EditText>(R.id.start_time)

        private val increase by bindView<ImageButton>(R.id.increase)
        private val decrease by bindView<ImageButton>(R.id.decrease)

        private val adapter by lazy {
            (activity as EditTimetableActivity)
                    .classList.adapter as ClassAdapter
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            notifyStartTimeChanged()

            increase.setOnClickListener {
                adapter.minIndex += 1
                notifyStartTimeChanged()
            }

            decrease.setOnClickListener {
                adapter.minIndex -= 1
                notifyStartTimeChanged()
            }

            startTime.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = Unit

                override fun beforeTextChanged(s: CharSequence?,
                                               start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        try {
                            adapter.minIndex = Integer.parseInt(s.toString())
                        } catch (ignore: NumberFormatException) {
                        }
                    }
                }
            })
        }

        private fun notifyStartTimeChanged() =
                startTime.setText(adapter.minIndex.toString())
    }
}
