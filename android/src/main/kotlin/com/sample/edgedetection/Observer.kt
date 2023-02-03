package com.example.lifecycle_observer

import android.util.Log
import androidx.lifecycle.*

class Observer:DefaultLifecycleObserver{
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d("MAIN", "onCreate: CObserver ")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("MAIN","onStart: SObserver")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.d("MAIN", "onPause: PObserver")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d("MAIN", "onResume: RObserver")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("MAIN", "onStop: STObserver")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d("MAIN", "onDestroy: DObserver")
    }
}