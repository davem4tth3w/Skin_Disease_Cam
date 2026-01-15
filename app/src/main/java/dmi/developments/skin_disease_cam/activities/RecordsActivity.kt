package dmi.developments.skin_disease_cam.activities

import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import dmi.developments.skin_disease_cam.data.entity.Result
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dmi.developments.skin_disease_cam.R
import dmi.developments.skin_disease_cam.viewmodel.ResultViewModel
import dmi.developments.skin_disease_cam.adapter.RecordAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem

@AndroidEntryPoint

class RecordsActivity : BottomNavActivity() {

    private val viewModel: ResultViewModel by viewModels()
    private lateinit var adapter: RecordAdapter
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records)
        setupBottomNav(R.id.nav_records)

        val recyclerView = findViewById<RecyclerView>(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RecordAdapter(
            onItemClick = { result ->
                val intent = Intent(this, ViewResultsActivity::class.java).apply {
                    putExtra("imagePath", result.imagePath)
                    putExtra("skindisease", result.skindisease)
                    putExtra("remedies", result.remedies)
                }
                startActivity(intent)
            },
            onMenuClick = { view, result -> showPopupMenu(view, result) },
            selectionChangeListener = { updateActionModeTitle() }
        )

        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.results.collectLatest { results ->
                adapter.submitList(results)
            }
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.select_options, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_close -> {
                    adapter.clearSelection()
                    mode.finish()
                }
                R.id.action_select_all -> {
                    adapter.selectAll()
                    updateActionModeTitle()
                }
                R.id.action_deselect_all -> {
                    adapter.clearSelection()
                    mode.finish()
                }
                R.id.action_delete_selected -> {
                    val selected = adapter.getSelectedResults()
                    AlertDialog.Builder(this@RecordsActivity)
                        .setTitle("Delete Records")
                        .setMessage("Are you sure you want to delete ${selected.size} record(s)?")
                        .setPositiveButton("Delete") { _, _ ->
                            lifecycleScope.launch {
                                selected.forEach { viewModel.deleteResult(it) }
                            }
                            adapter.clearSelection()
                            mode.finish()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    private fun updateActionModeTitle() {
        val count = adapter.getSelectedCount()
        if (count == 0) {
            actionMode?.finish()
        } else {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback)
            }
            actionMode?.title = "$count selected"
        }
    }

    private fun showPopupMenu(anchorView: View, result: Result) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.menu_popup, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view -> {
                    val intent = Intent(this, ViewResultsActivity::class.java).apply {
                        putExtra("imagePath", result.imagePath)
                        putExtra("skindisease", result.skindisease)
                        putExtra("remedies", result.remedies)
                    }
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Delete Record")
                        .setMessage("Are you sure you want to delete this record?")
                        .setPositiveButton("Delete") { _, _ ->
                            viewModel.deleteResult(result)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }
}