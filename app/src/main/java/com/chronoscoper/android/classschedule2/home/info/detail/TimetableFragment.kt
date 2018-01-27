package com.chronoscoper.android.classschedule2.home.info.detail

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.timetable.TimetableAdapter
import com.chronoscoper.android.classschedule2.sync.Info
import kotterknife.bindView
import org.parceler.Parcels

class TimetableFragment : Fragment() {
    companion object {
        private const val EXTRA_ITEM = "item"
        fun obtain(item: Info): TimetableFragment {
            return TimetableFragment().apply {
                val args = Bundle().apply { putParcelable(EXTRA_ITEM, Parcels.wrap(item)) }
                arguments = args
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_detail_timetable, container, false)

    private val timetableList by bindView<RecyclerView>(R.id.timetable_list)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val item = Parcels.unwrap<Info>(arguments.getParcelable(EXTRA_ITEM)) ?: return
        timetableList.adapter = TimetableAdapterWithTopInfo(context, item)
        timetableList.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }
}
