package com.cooper.moviemadness


object ConverterHelper{
  import _root_.android.view.View
  import View.OnClickListener
  implicit def funcToClicker(f:View => Unit):OnClickListener = 
    new OnClickListener(){ def onClick(v:View)=f.apply(v)}
  implicit def funcToClicker0(f:() => Unit):OnClickListener = 
    new OnClickListener() { def onClick(v:View)=f.apply}
}
