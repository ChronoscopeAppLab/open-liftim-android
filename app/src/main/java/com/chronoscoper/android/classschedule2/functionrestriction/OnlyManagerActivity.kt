package com.chronoscoper.android.classschedule2.functionrestriction

import android.os.Build
import android.os.Bundle
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import com.chronoscoper.android.classschedule2.transition.FabTransformTransition
import kotterknife.bindView

class OnlyManagerActivity : BaseActivity() {
    private val background by bindView<View>(R.id.background)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restriction_only_manager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.sharedElementEnterTransition = FabTransformTransition()
        }

        background.setOnClickListener {
            animateFinish()
        }
    }
}
