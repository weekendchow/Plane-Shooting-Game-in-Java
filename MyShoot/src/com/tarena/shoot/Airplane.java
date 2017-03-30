package com.tarena.shoot;
/** 敌机: 既是飞行物，也是敌人 */
public class Airplane extends FlyingObject implements Enemy  {
	private int speed = 2; //走步的步数
	
	/** Airplane构造方法 */
	public Airplane(){
		image = ShootGame.airplane; //图片
		width = image.getWidth();   //宽
		height = image.getHeight(); //高
		x = (int)(Math.random()*(ShootGame.WIDTH-this.width)); //0到屏幕宽减敌机宽之间的随机数
		y = -this.height; //负的敌机的高
	}
	
	/** 重写getScore() */
	public int getScore(){
		return 5; //打掉一个敌机得5分
	}
	/** 重写step() */
	public void step(){
		y += speed;  //y增
	}
	/** 重写outOfBounds() */
	public boolean outOfBounds(){
		return this.y > ShootGame.HEIGHT; //敌机的y大于屏幕的高为越界
	}
}





