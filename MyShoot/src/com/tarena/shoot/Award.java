package com.tarena.shoot;
/** 奖励 */
public interface Award {
	int DOUBLE_FIRE = 0; //火力值
	int LIFE = 1;  //命
	/** 获取奖励类型(0或1) */
	int getType();
}
