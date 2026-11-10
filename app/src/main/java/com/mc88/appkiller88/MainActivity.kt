package com.mc88.appkiller88

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString() }

        adapter = AppAdapter(apps, pm)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnKill).setOnClickListener {
            killSelectedApps()
        }
    }

    private fun killSelectedApps() {
        val prefs = getSharedPreferences("AK88Prefs", Context.MODE_PRIVATE)
        val selectedApps = prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        var killedCount = 0
        for (pkg in selectedApps) {
            am.killBackgroundProcesses(pkg)
            killedCount++
        }
        
        Toast.makeText(this, "Killed $killedCount apps in background", Toast.LENGTH_SHORT).show()
        adapter.notifyDataSetChanged()
    }

    inner class AppAdapter(private val apps: List<android.content.pm.ResolveInfo>, private val pm: PackageManager) :
        RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        private val prefs = getSharedPreferences("AK88Prefs", Context.MODE_PRIVATE)
        private val selectedApps = prefs.getStringSet("selected_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val icon: ImageView = v.findViewById(R.id.imgIcon)
            val name: TextView = v.findViewById(R.id.tvName)
            val checkBox: CheckBox = v.findViewById(R.id.checkBox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            val pkg = app.activityInfo.packageName
            holder.name.text = app.loadLabel(pm)
            holder.icon.setImageDrawable(app.loadIcon(pm))

            holder.checkBox.setOnCheckedChangeListener(null)
            holder.checkBox.isChecked = selectedApps.contains(pkg)

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedApps.add(pkg) else selectedApps.remove(pkg)
                prefs.edit().putStringSet("selected_apps", selectedApps).apply()
            }
        }

        override fun getItemCount() = apps.size
    }
}
