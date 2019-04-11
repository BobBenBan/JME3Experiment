package org.bensnonorg.musicmachine.core

fun runnable(run: ()->Unit):Runnable = Runnable {run()}

fun (()->Unit).asRunnable() = Runnable { invoke() }
