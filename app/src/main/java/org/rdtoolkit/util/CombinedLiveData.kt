package org.rdtoolkit.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class CombinedLiveData<T,V>(
        t : LiveData<T>,
        v : LiveData<V>
)  : MediatorLiveData<Pair<T, V>>() {
    private var tValue : T? = null
    private var vValue : V? = null
    init {
        addSource(t) {
            if (it != null ) {
                tValue = it
            }
            if (tValue != null && vValue != null) {
                this.value = Pair(tValue!!, vValue!!)
            }
        }

        addSource(v) {
            if (it != null ) {
                vValue = it
            }
            if (tValue != null && vValue != null) {
                this.value = Pair(tValue!!, vValue!!)
            }
        }
    }
}