package data

import androidx.compose.ui.geometry.Offset

interface BulbSwitcherActionListener {
    fun onPull(position: Offset)
    fun onRelease(position: Offset)
    fun onEndRelease()
    fun onClickListener()
}