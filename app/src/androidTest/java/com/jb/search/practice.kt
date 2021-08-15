package com.jb.search

import kotlinx.coroutines.CompletableDeferred


sealed class Action
data class Spin(val id:Int) :Action()
data class Done(val ack: CompletableDeferred<Boolean>):Action()
