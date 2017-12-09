/*
 * Copyright 2017 Chronoscope
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
package com.chronoscoper.android.classschedule2.home

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.chronoscoper.android.classschedule2.BaseActivity
import com.chronoscoper.android.classschedule2.R
import kotterknife.bindView

class HomeActivity : BaseActivity(), DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener {
    private val drawer by bindView<DrawerLayout>(R.id.activity_home)

    private val drawerMenu by bindView<NavigationView>(R.id.drawer_menu)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.content, HomeFragment())
                    .commit()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.open_drawer, R.string.close_drawer)
        drawer.addDrawerListener(toggle)
        drawer.addDrawerListener(this)
        drawerMenu.setNavigationItemSelectedListener(this)
        toggle.syncState()
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View?, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View?) {
    }

    override fun onDrawerOpened(drawerView: View?) {
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean = true
}
