package com.tarena.shoot;
import java.util.Random;

/** 蜜蜂: 既是飞行物，也是奖励 */
public class Bee extends FlyingObject implements Award {
	private int xSpeed = 1; //x走步的步数
	private int ySpeed = 2; //y走步的步数
	private int awardType;  //奖励类型(0为火力值，1为命)
	
	/** Bee构造方法 */
	public Bee(){
		image = ShootGame.bee; //图片
		width = image.getWidth();   //宽
		height = image.getHeight(); //高
		Random rand = new Random(); //随机数对象
		x = rand.nextInt(ShootGame.WIDTH-this.width); //0到屏幕宽减蜜蜂宽之间的随机数
		y = -this.height; //负的蜜蜂的高
		awardType = rand.nextInt(2); //0到1之间的随机数，0为火力值，1为命
	}
	
	/** 重写getType() */
	public int getType(){
		return awardType; //返回奖励类型
	}
	
	/** 重写step() */
	public void step(){
		x += xSpeed; //x增
		y += ySpeed; //y增
		if(x > ShootGame.WIDTH-this.width){ //若x大于屏幕宽减蜜蜂宽，则x减
			xSpeed = -1;
		}
		if(x < 0){  //若x小于0则x增
			xSpeed = 1;
		}
	}

	/** 重写outOfBounds() */
	public boolean outOfBounds(){
		return this.y > ShootGame.HEIGHT; //蜜蜂的y大于屏幕的高为越界
	}
}





