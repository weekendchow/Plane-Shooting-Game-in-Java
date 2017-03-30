package com.tarena.shoot;
/** 子弹: 是飞行物 */
public class Bullet extends FlyingObject {
	private int speed = 3; //走步的步数
	
	/** Bullet构造方法 x和y为子弹的xy坐标，随着英雄机动 */
	public Bullet(int x,int y){
		image = ShootGame.bullet; //图片
		width = image.getWidth();   //宽
		height = image.getHeight(); //高
		this.x = x;
		this.y = y;
	}
	
	/** 重写step() */
	public void step(){
		y -= speed; //y减
	}

	/** 重写outOfBounds() */
	public boolean outOfBounds(){
		return this.y < -this.height; //子弹的y小于负的子弹的高为越界
	}
}
