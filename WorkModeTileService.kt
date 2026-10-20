package com.focusflow.app.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.focusflow.app.R
import com.focusflow.app.data.FocusFlowRepository
import com.focusflow.app.widget.FocusFlowWidgetProvider

@RequiresApi(Build.VERSION_CODES.N)
class WorkModeTileService : TileService() {

    private lateinit var repository: FocusFlowRepository

    override fun onCreate() {
        super.onCreate()
        repository = FocusFlowRepository(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val isCurrentlyActive = repository.isWorkModeActive()
        val newState = !isCurrentlyActive
        repository.setWorkModeEnabled(newState)
        if (newState) {
            repository.applyStrictWorkModePreset()
        }
        updateTileState()
        FocusFlowWidgetProvider.updateAllWidgets(applicationContext)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isActive = repository.isWorkModeActive()

        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Work Mode"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isActive) "Active (Teams & Outlook)" else "Off"
        }
        tile.icon = Icon.createWithResource(this, R.mipmap.ic_launcher)
        tile.updateTile()
    }
}
