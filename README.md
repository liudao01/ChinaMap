# ChinaMap
android svg绘制可点击中国地图

[TOC]
![mark](http://ovji4jgcd.bkt.clouddn.com/blog/180405/H3h9miCjeh.gif)
## 思路
- 第一步   下载含有中国地图的  SVG https://www.amcharts.com/dl/javascript-maps/ 
- 第二步   用 http://inloop.github.io/svg2android/  网站 将svg资源转换成相应的 Android代码
- 第三步   利用Xml解析SVG的代码  封装成javaBean   最重要的得到Path
- 第四步   重写OnDraw方法  利用Path绘制中国地图
- 第五步   重写OnTouchEvent方法，记录手指触摸位置，判断这个位置是否坐落在某个省份上


找到svg的图  文件位置在这里 

ammap_3.21.2.free\ammap\maps\svg

找到china的文件 有两个 高精度和低精度随便哪个都行
			
	(1)Path指令解析如下所示：
	Path	
				M = moveto(M X,Y) ：将画笔移动到指定的坐标位置，相当于 android Path 里的moveTo()
				L = lineto(L X,Y) ：画直线到指定的坐标位置，相当于 android Path 里的lineTo()
				H = horizontal lineto(H X)：画水平线到指定的X坐标位置 
				V = vertical lineto(V Y)：画垂直线到指定的Y坐标位置 
				C = curveto(C X1,Y1,X2,Y2,ENDX,ENDY)：三次贝赛曲线 
				S = smooth curveto(S X2,Y2,ENDX,ENDY) 同样三次贝塞尔曲线，更平滑 
				Q = quadratic Belzier curve(Q X,Y,ENDX,ENDY)：二次贝赛曲线 
				T = smooth quadratic Belzier curveto(T ENDX,ENDY)：映射 同样二次贝塞尔曲线，更平滑 
				A = elliptical Arc(A RX,RY,XROTATION,FLAG1,FLAG2,X,Y)：弧线 ，相当于arcTo()
				Z = closepath()：关闭路径（会自动绘制链接起点和终点）
				
			    android:pathData="M541.02,336.29L541.71,336.09
				L542.54,337.38L543.77,338.27
				L543.53,338.58L545.92,338.99.8,350.12L561.12,349.61L562.97,349.6L563.89,349.94
				L563.48,350.21L563.6,351.15L562.98,351.84L562.99,353.94L562.28,353.68L562.06,3
				53.97L561.87,355.49L561.13,355.88L561.38,356.41L560.77,357.72L561.33,357.73
				。。。。。
				L562.06,359L563.49,358.5L563.75,357.85L564.17,358.09L564.64
				,361.19L565.52,361.68L564.51,362.21L564.67,363.38L565.17,363.21L565.35,364.41
				L566.19,364.53L566.23,365.29L567.26,365L568.99,365.25L569.63,364.91
				L539.3,337.63L539.84,336.78L540.31,336.88z" />
 
				
		//指令详情可以  参考 http://www.w3school.com.cn/svg/svg_intro.asp
		

		//SVG    --->  动画     
		地图资源可以在   https://www.amcharts.com/dl/javascript-maps/  下载
		里面包含世界各个国家的SVG地图   各个省份地图
		
		
		
		
		34  个Path
		
		 23个省、4个直辖市、2个特别行政区、5个自治区
		
		
		
		
		
		2014753635
		
		
		
		
		
		
---


### 分析地图

- 中国地图是34个省,那么实际上是通过34条path生成的.

那么把这些path封装成javabeen, 一条path就能代表一个省.

那么javabeen中应当有什么参数?

- path  路径
- 绘制的颜色
- 画的方法 draw
- 是否被选中的方法

起名  ProvinceBeen
---

# 代码具体实现

## 可交互的中国地图


### 第三步 利用Xml解析SVG的代码 封装成javaBean 最重要的得到Path


####  在自定义view中使用工具类

新建一个自定义View 名字叫做MyMapView

开启一个线程读取转换成的android使用的中国地图 svg文件.
第一步第二步 就不说了 这里需要注意 一定要去网站转换成android能够使用的代码 直接使用svg是网页版的 不是android版的

使用一个工具类PathParser 用作path 路径解析兼容类（兼容标准svg）

```

    //异步读取svg文件
    private void loadSVG(){
        itemList = new ArrayList<>();
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                InputStream  inputStream = context.getResources().openRawResource(R.raw.chinahigh);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//获取DocumentBuilderFactory
                DocumentBuilder builder = null;

                try {
                    builder = factory.newDocumentBuilder();//从factory中获取DocumentBuilder 实例
                    Document doc = builder.parse(inputStream);
                    Element rootElement = doc.getDocumentElement();//dom解析
                    NodeList items = rootElement.getElementsByTagName("path");//把所有包含path的节点拿出来
                    for (int i = 0; i < items.getLength(); i++) {
                        Element element = (Element) items.item(i);
                        String pathData = element.getAttribute("android:pathData");//读取path的数据
                        Path path = PathParser.createPathFromPathData(pathData);//通过工具类解析出Path
                        ProvinceBeen provinceBeen = new ProvinceBeen(path);
                        itemList.add(provinceBeen);
                        //重绘 
                        handler.sendEmptyMessage(0);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }
    
    //设置颜色
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (itemList != null) {
                int totalNumber = itemList.size();
                for (int i = 0; i < totalNumber; i++) {
                    int color = Color.WHITE;
                    //每隔四个省换个颜色 这里随机分配颜色
                    int flag = i % 4;
                    switch (flag) {
                        case 1:
                            color = colorArray[0];
                            break;
                        case 2:
                            color = colorArray[1];
                            break;
                        case 3:
                            color = colorArray[2];
                            break;
                        default:
                            color = colorArray[3];
                            break;
                    }
                    //设置省的颜色
                    itemList.get(i).setDrawColor(color);
                    postInvalidate();//刷新

                }

            }
        }
    };
```

- 需要把每个省画出来
```
  @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (proviceList != null) {
            canvas.scale(scale, scale);
            for (Provice item : proviceList) {
                if (item != selectItem) {
                    item.draw(canvas, paint, false);
                }
            }
            if (selectItem != null) {
                selectItem.draw(canvas, paint, true);
            }
        }
    }
```
把数据存入list
```
 ProvinceBeen provinceBeen = new ProvinceBeen(path);
                        itemList.add(provinceBeen);
```

### javabeen

```
package android.chinamap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @author liuml
 * @explain 省信息
 * @time 2018/2/28 15:03
 */

public class ProvinceBeen {

    //路径
    private Path path;
    //绘制的颜色
    private int drawColor;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(int drawColor) {
        this.drawColor = drawColor;
    }

    public ProvinceBeen(Path path) {
        this.path = path;
    }

    /**
     * @param canvas
     * @param paint
     * @param isSelect 是否被选中
     */
    public void draw(Canvas canvas, Paint paint, boolean isSelect) {
        //真正的绘制
        //区分选择和未被选择
        if (isSelect) {
            //绘制省份背景 因为是被选中的省份需要绘制有阴影
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);//设置黑色
            paint.setStyle(Paint.Style.FILL);
            /**
             * setShadowLayer 这将在主层下面绘制一个阴影层，并指定
             偏移，颜色，模糊半径。如果半径是0，那么阴影删除层。
             */
            paint.setShadowLayer(8, 0, 0, 0xfffffff);
            //直接绘制路径path
            canvas.drawPath(path, paint);

            //需要绘制两次  第一次绘制背景第二次绘制省份
            //绘制省份
            paint.clearShadowLayer();
            paint.setColor(drawColor);//省份是省份本身的颜色
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);//阴影
            canvas.drawPath(path, paint);
        } else {
            //没有被选中情况
            paint.clearShadowLayer();//清除阴影
            //绘制内容 填充部分
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);

            //绘制边界线
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xFFD0E8F4);
            canvas.drawPath(path, paint);

        }
    }

    /**
     * 判断是否点击区域
     *
     * @param x
     * @param y
     */
    public void isTouch(int x, int y) {


    }
}

```


- 到现在可以把图显示出来了


下面要做的是点击后的处理 主要处理点击范围是否在区域内

在Javabeen中 判断是否点击区域的方法
```

    /**
     * 判断是否点击区域
     *
     * @param x
     * @param y
     */
    public boolean isTouch(int x, int y) {

        //构建一个区域对象
        RectF rectF = new RectF();
        //把不规则区域  放入rectf中
        path.computeBounds(rectF,true);
        /**
         * 将该区域设置为路径和剪辑所描述的区域。
         如果结果区域为非空，则返回true。这产生一个地区
         这和路径所绘制的像素是一样的
         *(没有反混淆)。
         */
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

        //如果该区域包含指定的点，返回true
        return region.contains(x, y);

    }
```



下面就可以点击了

关于缩放比 参考了这个
https://blog.csdn.net/qq_18983205/article/details/77622961

    private float scale = 0f;
    private float mapWidth = 773.0f, mapHeight = 568.0f;
    
    ```
      @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        scale = Math.min(width/mapWidth, height/mapHeight);

    }

    ```




