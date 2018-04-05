package android.chinamap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

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
            paint.setStrokeWidth(5);
            paint.setColor(Color.BLACK);//设置黑色
            paint.setStyle(Paint.Style.FILL);
            /**
             * setShadowLayer 这将在主层下面绘制一个阴影层，并指定
             偏移，颜色，模糊半径。如果半径是0，那么阴影删除层。
             */
            paint.setShadowLayer(8, 0, 0, 0xFFFFFF00);
            //直接绘制路径path
            canvas.drawPath(path, paint);

            //需要绘制两次  第一次绘制背景第二次绘制省份
            //选中时，绘制省份
            paint.clearShadowLayer();
            paint.setColor(drawColor);//省份是省份本身的颜色
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(5);//阴影
            canvas.drawPath(path, paint);
        } else {
            //没有被选中情况
            paint.clearShadowLayer();//清除阴影
            //绘制内容 填充部分 非选中时，绘制描边效果
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(drawColor);
            canvas.drawPath(path, paint);

            //绘制边界线 //非选中时，绘制地图
            paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(0xFFD0E8F4);
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
}























