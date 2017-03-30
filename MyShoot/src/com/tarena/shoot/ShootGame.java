package com.tarena.shoot;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Font;

/** 主窗口 */
public class ShootGame extends JPanel {
	public static final int WIDTH = 400; //面板宽
	public static final int HEIGHT = 654;//面板高
	
	public static final int START = 0; //启动状态
	public static final int RUNNING = 1; //运行状态
	public static final int PAUSE = 2; //暂停状态
	public static final int GAME_OVER = 3; //游戏结束状态
	private int state = 0; //记录当前状态为启动状态
	
	public static BufferedImage background; //背景图
	public static BufferedImage start;    //启动图
	public static BufferedImage pause;    //暂停图
	public static BufferedImage gameover; //游戏结束图
	public static BufferedImage airplane; //敌机
	public static BufferedImage bee;      //小蜜蜂
	public static BufferedImage bullet;   //子弹
	public static BufferedImage hero0;    //英雄机1
	public static BufferedImage hero1;    //英雄机2
	
	private Hero hero = new Hero();  //英雄机对象
	private FlyingObject[] flyings = {}; //敌人数组
	private Bullet[] bullets = {}; //子弹数组
	
	static{ //加载静态资源
		try{
			background = ImageIO.read(ShootGame.class.getResource("background.png"));
			start = ImageIO.read(ShootGame.class.getResource("start.png"));
			pause = ImageIO.read(ShootGame.class.getResource("pause.png"));
			gameover = ImageIO.read(ShootGame.class.getResource("gameover.png"));
			airplane = ImageIO.read(ShootGame.class.getResource("airplane.png"));
			bee = ImageIO.read(ShootGame.class.getResource("bee.png"));
			bullet = ImageIO.read(ShootGame.class.getResource("bullet.png"));
			hero0 = ImageIO.read(ShootGame.class.getResource("hero0.png"));
			hero1 = ImageIO.read(ShootGame.class.getResource("hero1.png"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** 随机生成敌人(敌机+小蜜蜂)对象 */
	public static FlyingObject nextOne(){
		Random rand = new Random();
		int type = rand.nextInt(20); //生成0到19的随机数
		if(type == 0){
			return new Bee(); //type为0时返回蜜蜂对象
		}else{
			return new Airplane(); //type不为0时返回敌机对象
		}
	}
	
	private int flyEnteredIndex = 0; //敌人入场计数
	/** 敌人(敌机+小蜜蜂)入场 */
	public void enterAction(){ //10毫秒走一次
		flyEnteredIndex++; //每10个毫秒增一次
		if(flyEnteredIndex % 40 == 0){ //10*40=400毫秒走一次
			FlyingObject obj = nextOne(); //生成敌人对象
			flyings = Arrays.copyOf(flyings,flyings.length+1); //扩容
			flyings[flyings.length-1] = obj; //将对象放置在flyings数组的最后一位
		}
	}
	
	/** 飞行物(敌人+小蜜蜂+子弹+英雄机)走一步 */
	public void stepAction(){ //10毫秒走一次
		hero.step(); //英雄机走一步
		for(int i=0;i<flyings.length;i++){
			flyings[i].step(); //敌人走一步
		}
		for(int i=0;i<bullets.length;i++){
			bullets[i].step(); //子弹走一步
		}
	}
	
	int shootIndex = 0; //发射子弹计数器
	/** 英雄机发射子弹(子弹入场) */
	public void shootAction(){ //10毫秒走一次
		shootIndex++; //每10毫秒计数增1
		if(shootIndex % 30 == 0){ //10*30=300毫秒
			Bullet[] bs = hero.shoot(); //获取子弹数组
			bullets = Arrays.copyOf(bullets,bullets.length+bs.length); //扩容(bs有几个元素就扩大几个容量)
			System.arraycopy(bs,0,bullets,bullets.length-bs.length,bs.length); //数组的追加
		}
	}
	
	int score = 0; //得分
	/** 一堆子弹和一堆敌人撞 */
	public void bangAction(){
		for(int i=0;i<bullets.length;i++){ //遍历所有子弹
			bang(bullets[i]); //调用bang检测一个子弹和所有敌人撞
		}
	}
	/** 一发子弹和一堆敌人撞 */
	public void bang(Bullet b){
		int index = -1; //被撞敌人下标
		for(int i=0;i<flyings.length;i++){ //遍历所有敌人
			FlyingObject f = flyings[i]; //获取每一个敌人
			if(f.shootBy(b)){ //判断敌人是否与子弹撞上了
				index = i; //记录被撞敌人下标
				break; //退出循环
			}
		}
		if(index != -1){ //撞上了
			FlyingObject one = flyings[index]; //获取被撞的敌人对象
			if(one instanceof Enemy){ //若被撞对象为敌人
				Enemy e = (Enemy)one; //强转为敌人
				score += e.getScore();//增分
			}
			if(one instanceof Award){ //若被撞对象为奖励
				Award a = (Award)one; //强转为奖励
				int type = a.getType(); //获取奖励类型
				switch(type){           //判断奖励类型
				case Award.DOUBLE_FIRE: //若奖励为火力值
					hero.addDoubleFire(); //英雄机增火力
					break;
				case Award.LIFE:    //若奖励为命
					hero.addLife(); //英雄机增命
					break;
				}
			}
			
			//将被撞敌人对象与数组最后一个元素交换
			FlyingObject t = flyings[index];
			flyings[index] = flyings[flyings.length-1];
			flyings[flyings.length-1] = t;
			//缩容(删除最后一个元素，即删除被撞的敌人对象)
			flyings = Arrays.copyOf(flyings, flyings.length-1);
			
		}
	}
	
	/** 删除越界的飞行物(子弹和敌人(敌机+小蜜蜂)) */
	public void outOfBoundsAction(){
		int index = 0; //1.不越界数组下标 2.不越界敌人个数
		FlyingObject[] flyingLives = new FlyingObject[flyings.length]; //创建不越界敌人数组，每个元素默认为null
		for(int i=0;i<flyings.length;i++){ //遍历所有敌人
			FlyingObject f = flyings[i]; //获取每一个敌人
			if(!f.outOfBounds()){  //不越界
				flyingLives[index++] = f; //1.将不越界敌人保存到不越界敌人数组中
				                          //2.记录不越界敌人个数
			}
		}
		flyings = Arrays.copyOf(flyingLives,index); //将不越界敌人数组复制到原敌人数组的，index为复制的个数
		
		index = 0; //1.不越界数组下标 2.不越界子弹个数
		Bullet[] bulletLives = new Bullet[bullets.length]; //创建不越界子弹数组，每个元素默认为null
		for(int i=0;i<bullets.length;i++){ //遍历所有子弹
			Bullet b = bullets[i]; //获取每一个子弹
			if(!b.outOfBounds()){  //不越界
				bulletLives[index++] = b; //1.将不越界子弹保存到不越界子弹数组中
                                          //2.记录不越界子弹个数
			}
		}
		bullets = Arrays.copyOf(bulletLives, index); //将不越界子弹数组复制到原子弹数组的，index为复制的个数
	}
	
	/** 检测游戏是否结束 */
	public void checkGameOverAction(){
		if(isGameOver()){ //游戏结束
			state = GAME_OVER; //修改当前状态为游戏结束状态
		}
	}
	
	/** 判断游戏是否结束 */
	public boolean isGameOver(){
		for(int i=0;i<flyings.length;i++){ //遍历所有敌人
			int index = -1; //被撞敌人对象的下标
			FlyingObject f = flyings[i]; //获取每一个敌人
			if(hero.hit(f)){ //撞上了
				index = i;   //记录被撞敌人下标
				hero.subtractLife(); //减命
				hero.setDoubleFire(0);//火力值清零
			}
			if(index != -1){ //有被撞的对象
				//将被撞敌人对象与最后一个元素交换
				FlyingObject t = flyings[index];
				flyings[index] = flyings[flyings.length-1];
				flyings[flyings.length-1] = t;
				//缩容(删掉最后一个元素，即被撞敌人对象)
				flyings = Arrays.copyOf(flyings, flyings.length-1);
			}
		}
		return hero.getLife()<=0; //英雄机命数小于等于0则游戏结束
	}
	
	private Timer timer; //定时器
	private int intervel = 10; //时间间隔(10毫秒)
	/** 启动执行代码 */
	public void action(){
		//创建侦听器对象
		MouseAdapter l = new MouseAdapter(){
			/** 重写鼠标移动事件 */
			public void mouseMoved(MouseEvent e){
				if(state == RUNNING){ //运行状态下
					int x = e.getX(); //鼠标的x
					int y = e.getY(); //鼠标的y
					hero.moveTo(x, y); //英雄机移动
				}
			}
			/** 重写鼠标点击事件 */
			public void mouseClicked(MouseEvent e){
				switch(state){ //判断当前状态
				case START:  //启动状态时
					state = RUNNING;//改为运行运行
					break;
				case GAME_OVER: //游戏结束状态时
					score = 0;  //清理现场(所有数据归零)
					hero = new Hero();
					flyings = new FlyingObject[0];
					bullets = new Bullet[0];
					state = START; //改为启动状态
					break;
				}
			}
			/** 重写鼠标移出事件 */
			public void mouseExited(MouseEvent e){
				if(state == RUNNING){//当前状态为运行时
					state = PAUSE;   //改为暂停状态
				}
			}
			/** 重写鼠标移入事件 */
			public void mouseEntered(MouseEvent e){
				if(state == PAUSE){  //当前状态为暂停时
					state = RUNNING; //改为运行状态
				}
			}
		};
		this.addMouseListener(l); //处理鼠标操作事件
		this.addMouseMotionListener(l); //处理鼠标滑动事件
		
		
		timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){ //10毫秒走一次
				if(state == RUNNING){ //运行状态时
					enterAction(); //敌人入场
					stepAction();  //飞行物走一步
					shootAction(); //英雄机发射子弹(子弹入场)
					bangAction();  //子弹与敌人的碰撞
					outOfBoundsAction(); //删除越界的飞行物
					checkGameOverAction(); //检测游戏是否结束
				}
				repaint();     //重绘(调paint方法)
			}
		},intervel,intervel);
	}
	
	/** 重写paint() */
	public void paint(Graphics g){
		g.drawImage(background,0,0,null); //画背景图
		paintHero(g); //画英雄机对象
		paintFlyingObjects(g); //画敌人对象
		paintBullets(g); //画子弹对象
		paintScore(g); //画分和画命
		paintState(g); //画状态
	}
	/** 画状态 */
	public void paintState(Graphics g){
		switch(state){ //判断当前状态
		case START: //启动状态
			g.drawImage(start,0,0,null);
			break;
		case PAUSE: //暂停状态
			g.drawImage(pause,0,0,null);
			break;
		case GAME_OVER: //游戏结束状态
			g.drawImage(gameover,0,0,null);
			break;
		}
	}
	/** 画分和画命 */
	public void paintScore(Graphics g){
		g.setColor(new Color(0xFF0000)); //设置颜色(0xFF0000为纯红)
		g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20)); //设置字体(Font.SANS_SERIF为字体,Font.BOLD为字体样式,20为字号)
		g.drawString("SCORE: "+score,10,25); //画分
		g.drawString("LIFE: "+hero.getLife(),10,45); //画命
	}
	/** 画英雄机 */
	public void paintHero(Graphics g){
		g.drawImage(hero.image,hero.x,hero.y,null); //画英雄机对象
	}
	/** 画敌人 */
	public void paintFlyingObjects(Graphics g){
		for(int i=0;i<flyings.length;i++){ //遍历所有敌人
			FlyingObject f = flyings[i]; //获取每一个敌人
			g.drawImage(f.image,f.x,f.y,null); //画敌人对象
		}
	}
	/** 画子弹 */
	public void paintBullets(Graphics g){
		for(int i=0;i<bullets.length;i++){ //遍历所有子弹
			Bullet b = bullets[i]; //获取每一个子弹
			g.drawImage(b.image,b.x,b.y,null); //画子弹对象
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Fly"); //创建相框
		ShootGame game = new ShootGame(); //创建面板
		frame.add(game); //将面板添加到相框中
		
		frame.setSize(WIDTH, HEIGHT); //设置窗口的大小
		frame.setAlwaysOnTop(true); //设置窗口总在最上面
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //设置默认关闭操作(窗口关闭时退出程序)
		frame.setLocationRelativeTo(null); //设置窗口起始位置(居中)
		frame.setVisible(true); //1.设置窗口可见 2.尽快调用paint()方法
		
		game.action(); //启动执行
		
	}
}





