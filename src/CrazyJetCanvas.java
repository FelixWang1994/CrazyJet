
import java.util.Random;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;

public class CrazyJetCanvas extends GameCanvas implements CommandListener,Runnable {

	private Graphics gra;
	/************************用户飞机、5种对手飞机和3种奖励盒*************************/
	private Sprite myJet;
	private Sprite[] enemy1 = new Sprite[4];
	private Sprite[] enemy2 = new Sprite[6];
	private Sprite enemy3;                        //boss
	private Image[] enemyImg = new Image[5];      //储存图片
	private Sprite award;                         //存储奖励
	private Image[] awardImg = new Image[3];      //储存图片
	private Image map;                            //设置地图
	private Sprite[] bullet1 = new Sprite[15];    //子弹1
	private Image bullet1Img;
	private Sprite[] bullet2 = new Sprite[30];    //子弹2
	private Image bullet2Img;
	private Image bullet3Img;                     //boss的子弹
	private Sprite bullet3;
	private TiledLayer tlRoad;
	private TiledLayer tlWall;
	private LayerManager lm;//图层管理器
	private int Y = 0;
	/************************命令按钮***********************************/
	private Command cmdRestart = new Command("重新开始游戏", Command.SCREEN, 1);
	private Command cmdStart = new Command("开始游戏", Command.BACK, 1);
	private Command cmdStop = new Command("暂停游戏", Command.SCREEN, 1);
	/************************游戏线程***********************************/
	private Thread game;
	private boolean loop = true;

	
	private Random rnd = new Random();
	private int life = 3;         //三条命
	private int score = 0;        //分数
	private int awardFlag = 0;    //判断哪种奖励产生
	private int appear = 0;       //判断boss的闪电多久出现
	
	
	private int x;
	private int y;      //飞机的位置
	
	private int bulletType = 1;  // 1代表子弹1,2代表子弹2
	
	private Font font = null;
	
	private int num = 0;//记录发射炮弹1的数量
	private int num2 = 0;//记录发射炮弹2的数量

	public CrazyJetCanvas() {
		super(true);
		this.prepareResource();
		this.addCommand(cmdStart);
		this.setCommandListener(this);	
		
		gra = this.getGraphics();
		font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
	}

	public void prepareResource() {
		/************************载入图片***********************************/
		try {
			Image myImage = Image.createImage("/myJet1.png");
			map = Image.createImage("/map.png");
			myJet = new Sprite(myImage);
			tlRoad = new TiledLayer(20,20,map,map.getWidth()/2,map.getHeight());
			tlWall = new TiledLayer(20,20,map,map.getWidth()/2,map.getHeight());
			Y = map.getHeight()*20 - this.getHeight();
			lm = new LayerManager();
			lm.append(tlRoad);
			enemyImg[0] = Image.createImage("/enemy1.png");
			enemyImg[1] = Image.createImage("/enemy2.png");
			enemyImg[2] = Image.createImage("/enemy3.png");
			enemyImg[3] = Image.createImage("/enemy4.png");
			enemyImg[4] = Image.createImage("/enemy5.png");
			awardImg[0] = Image.createImage("/award1.png");
			awardImg[1] = Image.createImage("/award2.png");
			awardImg[2] = Image.createImage("/award3.png");
			bullet1Img = Image.createImage("/bullet3.png");
			bullet2Img = Image.createImage("/bullet4.png");
			bullet3Img = Image.createImage("/bulletBoss.png");
			bullet3 = new Sprite(bullet3Img);
			for(int i = 0; i < bullet1.length ; i ++){
				bullet1[i] = new Sprite(bullet1Img);
			}
			for (int i = 0; i < bullet2.length; i++) {
				
				bullet2[i] = new Sprite(bullet2Img);
			}
			for (int i = 0; i < enemy1.length / 2; i++) {
				enemy1[i] = new Sprite(enemyImg[0]);
				enemy1[enemy1.length - i - 1] = new Sprite(enemyImg[1]);
			}
			for (int i = 0; i < enemy2.length / 2; i++) {
				enemy2[i] = new Sprite(enemyImg[2]);
				enemy2[enemy2.length - i - 1] = new Sprite(enemyImg[3]);
			}
			enemy3 = new Sprite(enemyImg[4]);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		x =(this.getWidth()-myJet.getWidth())/2;
		y = this.getHeight() - myJet.getHeight();
		myJet.setPosition(x, y);
	}
	
	//设定飞机发射的炮弹1位置
	public void shoot(int i){
		bullet1[i].setPosition(x + myJet.getWidth()/2, y);
	}
	//设定飞机发射的炮弹2位置
	public void shoot2(int i){
		bullet2[i].setPosition(x + myJet.getWidth()/4, y);
		bullet2[i+15].setPosition(x + myJet.getWidth()/4*3, y);
	}
	private void setAward() {
		// TODO Auto-generated method stub
		int x, y;             
		int rndNum = rnd.nextInt(10);  //通过随机数来判断何种奖励
		if(rndNum == 1){
			award = new Sprite(awardImg[0]);
			awardFlag = 1;
		}else if(rndNum == 2){
			award = new Sprite(awardImg[1]);
			awardFlag = 2;
		}else if(rndNum == 3){
			award = new Sprite(awardImg[2]);
			awardFlag = 3;
		}else{
			awardFlag = 0;
		}
		if(awardFlag != 0){
			while (true) {
				x = rnd.nextInt((int) this.getWidth() - award.getWidth());	
				y = -rnd.nextInt(this.getHeight());		
				award.setPosition(x, y);			
				break;		
			}
		}
	}

	public void setEnemy1(int en) {
		int x, y;            
		while (true) {			
			x = rnd.nextInt((int) this.getWidth() - enemy1[en].getWidth());		
			y = -rnd.nextInt(enemy1[en].getHeight());			
			for (int j = 0; j < enemy1.length; j++) {				
				//不能让飞机和别的飞机重叠				
				if (j != en && enemy1 [j].collidesWith(enemy1[en], true)){					
					continue;				
				}								
			}			
			enemy1[en].setPosition(x, y);			
			break;		
		}	
	}
	
	public void setEnemy2(int en) {
		int x, y;              
		while (true) {			
			x = rnd.nextInt((int) this.getWidth() - enemy2[en].getWidth());		
			y = -rnd.nextInt(enemy2[en].getHeight());			
			for (int j = 0; j < enemy2.length; j++) {				
				//不能让飞机和别的飞机重叠				
				if (j != en && enemy2 [j].collidesWith(enemy2[en], true)){					
					continue;				
				}								
			}			
			enemy2[en].setPosition(x, y);			
			break;		
		}	
	}
	public void setEnemy3() {	
			enemy3.setPosition(0,17);		
	}

	public void commandAction(Command cmd, Displayable dis) {
		if (cmd == cmdStart) {
			loop = true;
			game = new Thread(this);
			game.start();
			this.removeCommand(cmdStart);
			this.removeCommand(cmdRestart);
			this.addCommand(cmdStop);
		} else if (cmd == cmdStop) {
			loop = false;
			game = null;
			this.removeCommand(cmdStop);
			this.addCommand(cmdStart);
			this.addCommand(cmdRestart);
		}
		else if(cmd == cmdRestart)
		{
			loop = true;
			score = 0;
			life = 3;
			bulletType = 1;
			appear = 0;
			game = new Thread(this);
			game.start();
			this.removeCommand(cmdRestart);
			this.removeCommand(cmdStart);
			this.addCommand(cmdStop);
		}
	}

	public void run() {
		while (loop) {
			drawScreen();
			int state = this.getKeyStates();
			switch(state){
			case GameCanvas.LEFT_PRESSED:
				if(x >= 10) x -= 10;
				break;
			case GameCanvas.RIGHT_PRESSED:
				if(x <= this.getWidth()-myJet.getWidth()) x += 10;
				break;	
			case GameCanvas.UP_PRESSED:
				if(y >= 10) y -= 10;
				break;
			case GameCanvas.DOWN_PRESSED:
				if(y <= this.getHeight()-myJet.getHeight()) y += 10; 
				break;	
			case GameCanvas.FIRE_PRESSED:
				if(bulletType == 1)
				{
					if(num == 15)
						num = 0;
					shoot(num);
					num++;
				}
				else
				{
					if(num2 == 15)
						num2 = 0;
					shoot2(num2);
					num2++;
				}
				break;
			}
			myJet.setPosition(x, y);
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
	}

	public void drawScreen() {		
		//画背景
		drawSky();	
		//画出当前分数
		gra.setColor(255,0,0);
		gra.setFont(font);		
		gra.drawString("当前分数："+score, this.getWidth()/2, 0, Graphics.TOP|Graphics.HCENTER);
		gra.drawString("当前命数："+life, 0, this.getHeight(), Graphics.BOTTOM|Graphics.LEFT);
		//敌机下移	
		if(score % 10000 < 3000){
			for (int i = 0; i < enemy1.length; i++) {
				enemy1[i].move(0, 10);
				enemy1[i].paint(gra);
				if (enemy1[i].getY() > this.getHeight()){
					setEnemy1(i);
				}				
				//判断是否碰撞
				check(enemy1[i]);
			}
		}else if(score % 10000 < 8000){
			for (int i = 0; i < enemy2.length; i++) {
				enemy2[i].move(0, 15);
				enemy2[i].paint(gra);
				if (enemy2[i].getY() > this.getHeight()){
					setEnemy2(i);
				}				
				//判断是否碰撞
				check(enemy2[i]);
			}
		}else{                                  //boss不会移动
				enemy3.setPosition(0,17);
				if(appear % 10 == 0){         //每1秒boss会发一次闪电，闪电位置随机
					int i = rnd.nextInt(enemy3.getWidth());
					bullet3.setPosition(i, enemy3.getHeight());
					bullet3.paint(gra);
					check(bullet3);
				}
				appear ++;
			    enemy3.paint(gra);
				checkBoss(enemy3);
				
		}
		if(awardFlag == 1 || awardFlag == 2 || awardFlag == 3){
			award.move(0, 10);
			award.paint(gra);	
			if (award.getY() > this.getHeight()){
				setAward();
			}	
			checkAward(award);
		}else{
			setAward();
		}
		//炮弹上移
		if(bulletType == 1)//发射第一种炮弹
		{
			for (int i = 0; i < 15; i++) {
				bullet1[i].move(0, -15);
				bullet1[i].paint(gra);
			}
		}
		else
		{
			for (int i = 0; i < 30; i++) {
				bullet2[i].move(0, -15);
				bullet2[i].paint(gra);
			}
		}
		myJet.paint(gra);
		this.flushGraphics();
	}

	private void checkBoss(Sprite en) {  //boss必须击中50次才会死
		// TODO Auto-generated method stub
		int times = 0;   //击中次数
		if (myJet.collidesWith(en, true)) {
			life --;
		}
		if(bulletType == 1)//用第一种炮弹射击
		{
			for(int i = 0;i < bullet1.length;i++){
			if(bullet1[i].collidesWith(en, true))
			{
				score += 40 ;
				times ++;
				if(times >= 50){
					en.setPosition(-en.getWidth(), this.getHeight()+10);
				}
				bullet1[i].setPosition(this.getWidth(), 0);
			}
			}
		}
		else//用第二种炮弹射击
		{
			for(int i =0; i < bullet2.length;i ++){
			if(bullet2[i].collidesWith(en, true))
			{
				score += 40 ;
				times ++;
				if(times >= 50){
					en.setPosition(-en.getWidth(), this.getHeight()+10);
				}
				bullet2[i].setPosition(this.getWidth(), 0);
			}
			}
		}
		if(life==0){
			loop = false;
			game = null;
			//将界面背景用灰色清空
			gra.setColor(120, 120, 120);
			gra.fillRect(0, 0, this.getWidth(), this.getHeight());
			gra.setColor(255,0,0);
			gra.setFont(font);		
			gra.drawString("您输了", this.getWidth()/2, 0, Graphics.TOP|Graphics.HCENTER);
			gra.drawString("您的得分是："+score, this.getWidth()/2, 50, Graphics.TOP|Graphics.HCENTER);
		    this.removeCommand(cmdStart);
		    this.removeCommand(cmdStop);
		    this.addCommand(cmdRestart);
		}
	}

	public void drawSky() {
			int[][] cells = new int[20][20];
			for(int i=0;i<20;i++){
				for(int j=0;j<20;j++){
						cells[i][j]=1;
						tlRoad.setCell(j, i, cells[i][j]);
					}
			}
			gra.setColor(255,255,255);
			gra.fillRect(0,0,this.getWidth(),this.getHeight());
			//通过LayerManager显示
			lm.setViewWindow(0,Y,this.getWidth(),this.getHeight());	
			lm.paint(gra, 0,0);	//将图层管理器内的图层画到界面的左上角0,0位置
			//this.flushGraphics();
			Y = Y - 5;
			if(Y < 0)//当Y小于0时重设Y，使道路循环滚动
				Y = map.getHeight()*20 - this.getHeight();
	}

	public void checkAward(Sprite en){
		if(myJet.collidesWith(en, true)){
			if(awardFlag == 1){          //第一种奖励一条命
				life ++;
				en.setPosition(-en.getWidth(), this.getHeight()+10);
			}else if(awardFlag == 2){    //第二种奖励1000分
				score += 1000;
				en.setPosition(-en.getWidth(), this.getHeight()+10);
			}else if(awardFlag == 3){                       //第三种奖励换成激光
				if(bulletType == 1)
					bulletType = 2; 
				else
					bulletType = 1;
				en.setPosition(-en.getWidth(), this.getHeight()+10);
			}
		}
	}
	public void check(Sprite en) {
		if (myJet.collidesWith(en, true)) {
			life --;
			en.move(-en.getWidth(), this.getHeight()+10);
		}
		if(bulletType == 1)//用第一种炮弹射击
		{
			if(score % 10000 < 3000){
				for(int i = 0;i<15;i++)
				{
					if(bullet1[i].collidesWith(en, true))
					{
						score += 100 ;
						en.setPosition(-en.getWidth(), this.getHeight()+10);
						bullet1[i].setPosition(this.getWidth(), 0);
					}
				}
			}else if(score % 10000 < 8000){
				for(int i = 0;i<15;i++)
				{
					if(bullet1[i].collidesWith(en, true))
					{
						score += 300 ;
						en.setPosition(-en.getWidth(), this.getHeight()+10);
						bullet1[i].setPosition(this.getWidth(), 0);
					}
				}
			}
		}else//用第二种炮弹射击
		{
			if(score % 10000 < 3000){
				for(int i = 0;i<30;i++)
				{
					if(bullet2[i].collidesWith(en, true))
					{
						score += 100 ;
						en.setPosition(-en.getWidth(), this.getHeight()+10);
						bullet2[i].setPosition(this.getWidth(), 0);
					}
				}
			}else if(score % 10000 < 8000){
				for(int i = 0;i<30;i++)
				{
					if(bullet2[i].collidesWith(en, true))
					{
						score += 300 ;
						en.setPosition(-en.getWidth(), this.getHeight()+10);
						bullet2[i].setPosition(this.getWidth(), 0);
					}
				}
			}
		}
		if(life==0){
			loop = false;
			game = null;
			//将界面背景用灰色清空
			gra.setColor(120, 120, 120);
			gra.fillRect(0, 0, this.getWidth(), this.getHeight());
			gra.setColor(255,0,0);
			gra.setFont(font);		
			gra.drawString("您输了", this.getWidth()/2, 0, Graphics.TOP|Graphics.HCENTER);
			gra.drawString("您的得分是："+score, this.getWidth()/2, 50, Graphics.TOP|Graphics.HCENTER);
		    this.removeCommand(cmdStart);
		    this.removeCommand(cmdStop);
		    this.addCommand(cmdRestart);
		}
	}
}
