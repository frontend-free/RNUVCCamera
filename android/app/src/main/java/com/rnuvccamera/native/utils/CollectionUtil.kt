package com.rnuvccamera.native.utils

object CollectionUtil {
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }

    fun isEmpty(map: Map<*, *>?): Boolean {
        return map == null || map.isEmpty()
    }

    fun isEmpty(array: Array<*>?): Boolean {
        return array == null || array.isEmpty()
    }

    fun hasIndex(index: Int, collection: Collection<*>?): Boolean {
        return !isEmpty(collection) && index >= 0 && index < collection!!.size
    }

    fun hasIndex(index: Int, array: Array<*>?): Boolean {
        return !isEmpty(array) && index >= 0 && index < array!!.size
    }
} 