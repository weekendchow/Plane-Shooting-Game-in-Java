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

/** ������ */
public class ShootGame extends JPanel {
	public static final int WIDTH = 400; //����
	public static final int HEIGHT = 654;//����
	
	public static final int START = 0; //����״̬
	public static final int RUNNING = 1; //����״̬
	public static final int PAUSE = 2; //��ͣ״̬
	public static final int GAME_OVER = 3; //��Ϸ����״̬
	private int state = 0; //��¼��ǰ״̬Ϊ����״̬
	
	public static BufferedImage background; //����ͼ
	public static BufferedImage start;    //����ͼ
	public static BufferedImage pause;    //��ͣͼ
	public static BufferedImage gameover; //��Ϸ����ͼ
	public static BufferedImage airplane; //�л�
	public static BufferedImage bee;      //С�۷�
	public static BufferedImage bullet;   //�ӵ�
	public static BufferedImage hero0;    //Ӣ�ۻ�1
	public static BufferedImage hero1;    //Ӣ�ۻ�2
	
	private Hero hero = new Hero();  //Ӣ�ۻ�����
	private FlyingObject[] flyings = {}; //��������
	private Bullet[] bullets = {}; //�ӵ�����
	
	static{ //���ؾ�̬��Դ
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
	
	/** ������ɵ���(�л�+С�۷�)���� */
	public static FlyingObject nextOne(){
		Random rand = new Random();
		int type = rand.nextInt(20); //����0��19�������
		if(type == 0){
			return new Bee(); //typeΪ0ʱ�����۷����
		}else{
			return new Airplane(); //type��Ϊ0ʱ���صл�����
		}
	}
	
	private int flyEnteredIndex = 0; //�����볡����
	/** ����(�л�+С�۷�)�볡 */
	public void enterAction(){ //10������һ��
		flyEnteredIndex++; //ÿ10��������һ��
		if(flyEnteredIndex % 40 == 0){ //10*40=400������һ��
			FlyingObject obj = nextOne(); //���ɵ��˶���
			flyings = Arrays.copyOf(flyings,flyings.length+1); //����
			flyings[flyings.length-1] = obj; //�����������flyings��������һλ
		}
	}
	
	/** ������(����+С�۷�+�ӵ�+Ӣ�ۻ�)��һ�� */
	public void stepAction(){ //10������һ��
		hero.step(); //Ӣ�ۻ���һ��
		for(int i=0;i<flyings.length;i++){
			flyings[i].step(); //������һ��
		}
		for(int i=0;i<bullets.length;i++){
			bullets[i].step(); //�ӵ���һ��
		}
	}
	
	int shootIndex = 0; //�����ӵ�������
	/** Ӣ�ۻ������ӵ�(�ӵ��볡) */
	public void shootAction(){ //10������һ��
		shootIndex++; //ÿ10���������1
		if(shootIndex % 30 == 0){ //10*30=300����
			Bullet[] bs = hero.shoot(); //��ȡ�ӵ�����
			bullets = Arrays.copyOf(bullets,bullets.length+bs.length); //����(bs�м���Ԫ�ؾ����󼸸�����)
			System.arraycopy(bs,0,bullets,bullets.length-bs.length,bs.length); //�����׷��
		}
	}
	
	int score = 0; //�÷�
	/** һ���ӵ���һ�ѵ���ײ */
	public void bangAction(){
		for(int i=0;i<bullets.length;i++){ //���������ӵ�
			bang(bullets[i]); //����bang���һ���ӵ������е���ײ
		}
	}
	/** һ���ӵ���һ�ѵ���ײ */
	public void bang(Bullet b){
		int index = -1; //��ײ�����±�
		for(int i=0;i<flyings.length;i++){ //�������е���
			FlyingObject f = flyings[i]; //��ȡÿһ������
			if(f.shootBy(b)){ //�жϵ����Ƿ����ӵ�ײ����
				index = i; //��¼��ײ�����±�
				break; //�˳�ѭ��
			}
		}
		if(index != -1){ //ײ����
			FlyingObject one = flyings[index]; //��ȡ��ײ�ĵ��˶���
			if(one instanceof Enemy){ //����ײ����Ϊ����
				Enemy e = (Enemy)one; //ǿתΪ����
				score += e.getScore();//����
			}
			if(one instanceof Award){ //����ײ����Ϊ����
				Award a = (Award)one; //ǿתΪ����
				int type = a.getType(); //��ȡ��������
				switch(type){           //�жϽ�������
				case Award.DOUBLE_FIRE: //������Ϊ����ֵ
					hero.addDoubleFire(); //Ӣ�ۻ�������
					break;
				case Award.LIFE:    //������Ϊ��
					hero.addLife(); //Ӣ�ۻ�����
					break;
				}
			}
			
			//����ײ���˶������������һ��Ԫ�ؽ���
			FlyingObject t = flyings[index];
			flyings[index] = flyings[flyings.length-1];
			flyings[flyings.length-1] = t;
			//����(ɾ�����һ��Ԫ�أ���ɾ����ײ�ĵ��˶���)
			flyings = Arrays.copyOf(flyings, flyings.length-1);
			
		}
	}
	
	/** ɾ��Խ��ķ�����(�ӵ��͵���(�л�+С�۷�)) */
	public void outOfBoundsAction(){
		int index = 0; //1.��Խ�������±� 2.��Խ����˸���
		FlyingObject[] flyingLives = new FlyingObject[flyings.length]; //������Խ��������飬ÿ��Ԫ��Ĭ��Ϊnull
		for(int i=0;i<flyings.length;i++){ //�������е���
			FlyingObject f = flyings[i]; //��ȡÿһ������
			if(!f.outOfBounds()){  //��Խ��
				flyingLives[index++] = f; //1.����Խ����˱��浽��Խ�����������
				                          //2.��¼��Խ����˸���
			}
		}
		flyings = Arrays.copyOf(flyingLives,index); //����Խ��������鸴�Ƶ�ԭ��������ģ�indexΪ���Ƶĸ���
		
		index = 0; //1.��Խ�������±� 2.��Խ���ӵ�����
		Bullet[] bulletLives = new Bullet[bullets.length]; //������Խ���ӵ����飬ÿ��Ԫ��Ĭ��Ϊnull
		for(int i=0;i<bullets.length;i++){ //���������ӵ�
			Bullet b = bullets[i]; //��ȡÿһ���ӵ�
			if(!b.outOfBounds()){  //��Խ��
				bulletLives[index++] = b; //1.����Խ���ӵ����浽��Խ���ӵ�������
                                          //2.��¼��Խ���ӵ�����
			}
		}
		bullets = Arrays.copyOf(bulletLives, index); //����Խ���ӵ����鸴�Ƶ�ԭ�ӵ�����ģ�indexΪ���Ƶĸ���
	}
	
	/** �����Ϸ�Ƿ���� */
	public void checkGameOverAction(){
		if(isGameOver()){ //��Ϸ����
			state = GAME_OVER; //�޸ĵ�ǰ״̬Ϊ��Ϸ����״̬
		}
	}
	
	/** �ж���Ϸ�Ƿ���� */
	public boolean isGameOver(){
		for(int i=0;i<flyings.length;i++){ //�������е���
			int index = -1; //��ײ���˶�����±�
			FlyingObject f = flyings[i]; //��ȡÿһ������
			if(hero.hit(f)){ //ײ����
				index = i;   //��¼��ײ�����±�
				hero.subtractLife(); //����
				hero.setDoubleFire(0);//����ֵ����
			}
			if(index != -1){ //�б�ײ�Ķ���
				//����ײ���˶��������һ��Ԫ�ؽ���
				FlyingObject t = flyings[index];
				flyings[index] = flyings[flyings.length-1];
				flyings[flyings.length-1] = t;
				//����(ɾ�����һ��Ԫ�أ�����ײ���˶���)
				flyings = Arrays.copyOf(flyings, flyings.length-1);
			}
		}
		return hero.getLife()<=0; //Ӣ�ۻ�����С�ڵ���0����Ϸ����
	}
	
	private Timer timer; //��ʱ��
	private int intervel = 10; //ʱ����(10����)
	/** ����ִ�д��� */
	public void action(){
		//��������������
		MouseAdapter l = new MouseAdapter(){
			/** ��д����ƶ��¼� */
			public void mouseMoved(MouseEvent e){
				if(state == RUNNING){ //����״̬��
					int x = e.getX(); //����x
					int y = e.getY(); //����y
					hero.moveTo(x, y); //Ӣ�ۻ��ƶ�
				}
			}
			/** ��д������¼� */
			public void mouseClicked(MouseEvent e){
				switch(state){ //�жϵ�ǰ״̬
				case START:  //����״̬ʱ
					state = RUNNING;//��Ϊ��������
					break;
				case GAME_OVER: //��Ϸ����״̬ʱ
					score = 0;  //�����ֳ�(�������ݹ���)
					hero = new Hero();
					flyings = new FlyingObject[0];
					bullets = new Bullet[0];
					state = START; //��Ϊ����״̬
					break;
				}
			}
			/** ��д����Ƴ��¼� */
			public void mouseExited(MouseEvent e){
				if(state == RUNNING){//��ǰ״̬Ϊ����ʱ
					state = PAUSE;   //��Ϊ��ͣ״̬
				}
			}
			/** ��д��������¼� */
			public void mouseEntered(MouseEvent e){
				if(state == PAUSE){  //��ǰ״̬Ϊ��ͣʱ
					state = RUNNING; //��Ϊ����״̬
				}
			}
		};
		this.addMouseListener(l); //�����������¼�
		this.addMouseMotionListener(l); //������껬���¼�
		
		
		timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){ //10������һ��
				if(state == RUNNING){ //����״̬ʱ
					enterAction(); //�����볡
					stepAction();  //��������һ��
					shootAction(); //Ӣ�ۻ������ӵ�(�ӵ��볡)
					bangAction();  //�ӵ�����˵���ײ
					outOfBoundsAction(); //ɾ��Խ��ķ�����
					checkGameOverAction(); //�����Ϸ�Ƿ����
				}
				repaint();     //�ػ�(��paint����)
			}
		},intervel,intervel);
	}
	
	/** ��дpaint() */
	public void paint(Graphics g){
		g.drawImage(background,0,0,null); //������ͼ
		paintHero(g); //��Ӣ�ۻ�����
		paintFlyingObjects(g); //�����˶���
		paintBullets(g); //���ӵ�����
		paintScore(g); //���ֺͻ���
		paintState(g); //��״̬
	}
	/** ��״̬ */
	public void paintState(Graphics g){
		switch(state){ //�жϵ�ǰ״̬
		case START: //����״̬
			g.drawImage(start,0,0,null);
			break;
		case PAUSE: //��ͣ״̬
			g.drawImage(pause,0,0,null);
			break;
		case GAME_OVER: //��Ϸ����״̬
			g.drawImage(gameover,0,0,null);
			break;
		}
	}
	/** ���ֺͻ��� */
	public void paintScore(Graphics g){
		g.setColor(new Color(0xFF0000)); //������ɫ(0xFF0000Ϊ����)
		g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,20)); //��������(Font.SANS_SERIFΪ����,Font.BOLDΪ������ʽ,20Ϊ�ֺ�)
		g.drawString("SCORE: "+score,10,25); //����
		g.drawString("LIFE: "+hero.getLife(),10,45); //����
	}
	/** ��Ӣ�ۻ� */
	public void paintHero(Graphics g){
		g.drawImage(hero.image,hero.x,hero.y,null); //��Ӣ�ۻ�����
	}
	/** ������ */
	public void paintFlyingObjects(Graphics g){
		for(int i=0;i<flyings.length;i++){ //�������е���
			FlyingObject f = flyings[i]; //��ȡÿһ������
			g.drawImage(f.image,f.x,f.y,null); //�����˶���
		}
	}
	/** ���ӵ� */
	public void paintBullets(Graphics g){
		for(int i=0;i<bullets.length;i++){ //���������ӵ�
			Bullet b = bullets[i]; //��ȡÿһ���ӵ�
			g.drawImage(b.image,b.x,b.y,null); //���ӵ�����
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Fly"); //�������
		ShootGame game = new ShootGame(); //�������
		frame.add(game); //�������ӵ������
		
		frame.setSize(WIDTH, HEIGHT); //���ô��ڵĴ�С
		frame.setAlwaysOnTop(true); //���ô�������������
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //����Ĭ�Ϲرղ���(���ڹر�ʱ�˳�����)
		frame.setLocationRelativeTo(null); //���ô�����ʼλ��(����)
		frame.setVisible(true); //1.���ô��ڿɼ� 2.�������paint()����
		
		game.action(); //����ִ��
		
	}
}





