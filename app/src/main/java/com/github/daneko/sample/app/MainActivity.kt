package com.github.daneko.sample.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import arrow.core.Either
import arrow.effects.fix
import arrow.effects.rx2.unsafeRunAsync
import com.github.daneko.sample.app.databinding.ActivityMainBinding
import com.github.daneko.sample.domain.*


class MainActivity : AppCompatActivity() {


    private lateinit var adapter: PhotoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {

            val rxPrefLogic = Logic.useRxJavaAndPreference(this@MainActivity)
            val rxRoomLogic = Logic.useRxJavaAndRoom(this@MainActivity)
            val ioPrefLogic = Logic.useIoAndPreference(this@MainActivity)
            val ioRoomLogic = Logic.useIoAndRoom(this@MainActivity)

            adapter = PhotoListAdapter()

            button.setOnClickListener {
                val userId =
                    editText.text.toString().toIntOrNull()
                        ?: return@setOnClickListener

                val isRx = radioRx.isChecked
                val isPreference = radioPref.isChecked

                when {
                    isRx && isPreference ->
                        rxPrefLogic.execute(UserId(userId)).unsafeRunAsync(process())
                    isRx && !isPreference ->
                        rxRoomLogic.execute(UserId(userId)).unsafeRunAsync(process())
                    !isRx && isPreference ->
                        ioPrefLogic.execute(UserId(userId)).fix().unsafeRunAsync(process())
                    !isRx && !isPreference ->
                        ioRoomLogic.execute(UserId(userId)).fix().unsafeRunAsync(process())
                    else -> throw RuntimeException("not reach here")
                }
            }

            recyclerView.apply {
                val divider = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                addItemDecoration(divider)
                adapter = this@MainActivity.adapter
            }
        }
    }

    private fun process(): (Either<Throwable, Either<GetPhotoListErrors, GetPhotoResult>>) -> Unit = { result ->
        result.fold(
            ifLeft = { Log.e("test", "unknown error", it) },
            ifRight = {
                it.bimap(
                    this@MainActivity::onError,
                    this@MainActivity::onSuccess
                )
            }
        )
    }

    private fun onError(errors: GetPhotoListErrors) {
        Log.w("test", "onError", errors)

        val msg = when (errors) {
            is GetPhotoListErrors.UserNotFound -> "User Not Found"
            is GetPhotoListErrors.UserHasNoPhoto -> "Not Found Photo"
            is GetPhotoListErrors.StoreError -> "Error Save Photo"
        }

        runOnUiThread {
            Toast.makeText(
                this,
                msg,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onSuccess(data: Pair<User, List<Photo>>) {
        Log.d("test", "onSuccess $data")

        runOnUiThread {
            adapter.submitList(data.second.map(::PhotoCell))
        }
    }
}
