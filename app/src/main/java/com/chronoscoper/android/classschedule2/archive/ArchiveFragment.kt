package com.chronoscoper.android.classschedule2.archive

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.home.info.InfoRecyclerViewAdapter
import com.chronoscoper.android.classschedule2.sync.Info
import com.chronoscoper.android.classschedule2.sync.LiftimContext
import kotterknife.bindView

class ArchiveFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_archive, container, false)

    private val list by bindView<RecyclerView>(R.id.list)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        list.adapter = Adapter(context)
        list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    private class Adapter(context: Context) : InfoRecyclerViewAdapter(context) {
        override fun loadData(): Iterable<Info> =
                LiftimContext.getOrmaDatabase().selectFromInfo()
                        .liftimCodeEq(LiftimContext.getLiftimCode())
                        .deletedEq(true)
    }
}
