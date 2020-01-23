package com.example.shakeme

import android.app.ListActivity
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.View
import android.view.ViewGroup
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import java.util.*
import android.widget.*
import com.example.shakeme.R


class ChooserActivity : ListActivity() {

    private var adapter: AppAdapter? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)

        val pm: PackageManager = packageManager
        val main = Intent(Intent.ACTION_MAIN, null)

        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables: MutableList<ResolveInfo> = pm.queryIntentActivities(main, 0)
        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(pm)
        )
        adapter = AppAdapter(pm, launchables)
        listAdapter = adapter
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        val launchable: ResolveInfo? = adapter?.getItem(position)
        val activity: ActivityInfo = launchable!!.activityInfo
        val name = ComponentName(
            activity.applicationInfo.packageName,
            activity.name
        )
        val packName:String= name.packageName
        val intentMessage = Intent()

        intentMessage.putExtra("MESSAGE_package_name", packName)
        setResult(1, intentMessage)

        finish()
    }

    internal inner class AppAdapter(private val pm: PackageManager, apps: List<ResolveInfo>) :
        ArrayAdapter<ResolveInfo>(this@ChooserActivity, R.layout.row, apps) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            var convertV: View? = convertView

            if (convertV == null)
                convertV = newView(parent)

            bindView(position, convertV)

            return convertV
        }

        private fun newView(parent: ViewGroup): View {
            return layoutInflater.inflate(R.layout.row, parent, false)
        }

        private fun bindView(position: Int, row: View) {
            val label: TextView = row.findViewById(R.id.label) as TextView

            label.text = getItem(position)!!.loadLabel(pm)

            val icon: ImageView = row.findViewById<View>(R.id.icon) as ImageView

            icon.setImageDrawable(getItem(position)!!.loadIcon(pm))
        }
    }
}