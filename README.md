一个画板的demo
=============

<p>先来看效果图！</p>
<img src="https://github.com/luying6/LuDrawBoard/tree/master/gif/gif-2.gif"/>
<p>这次没有写框架而是直接封装了一个自定义画板view的工具类，写框架的话一个太累，二个我这个人很懒基本不维护，所以想来想去还是直接封装一个工具类</p>
<p>代码里面都加了注释纯中文，画笔和橡皮擦的宽度是写死的，等有时间再去写个public方法给外部调用</p>
<p>拓展--》用socket长链接搞成时时的用于一些网络会议展示什么的，也可以在app里面架设一个server用于wife传图附带涂鸦效果功能，或者游戏你画我猜之类的，总之应用场景很多！</p>

<p>使用起来也很简单:</p>
  <com.luying.ludrawboard.DrawBoardView
          android:id="@+id/drawBoard"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:background="@drawable/background" />
          
          
 <p>然后在activity里面直接调用就可以了</p>
 <p>画笔和橡皮擦切换用setModel(Model model) 方法</p>
 <p>Model使用的是enum枚举，效率有点坑但是为了好看所以代码里先这么用，可以自己改为final。</p>
 <p>DRAW:画笔</p>
 <p>ERASER:橡皮擦</p>
 
 
 
<p>undo:撤销</p>
<p>redo:撤销上一次撤销</p>
<p>clear:清除所有</p>
<p>buildBitmap:把canvas转换为bitmap，需要保存图片的直接调用这个方法就行，返回的是bitmap</p>
<p>注意：撤销和反撤销现在只能各执行20次，超过20次递加的时候会在index为0的时候递减，这个点可以直接在代码里修改List的大小，或者就不设置大小</p>




