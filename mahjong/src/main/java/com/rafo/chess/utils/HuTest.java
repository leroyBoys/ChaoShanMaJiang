package com.rafo.chess.utils;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.model.battle.PlayerCardInfo;

import java.io.*;
import java.util.*;

public class HuTest
{
	/**
	 * 按行读文件
	 *
	 * @param file
	 * @return
	 */
	public static List<String> readLines(File file) {
		BufferedReader bf = null;
		List<String> lines = new ArrayList<String>();
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
			while (bf.ready()) {
				String name = bf.readLine().trim();
				lines.add(name);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bf != null) {
					bf.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lines;
	}

	public static void main(String[] ard){
		System.out.println(DateTimeUtil.getDateString(new Date(),DateTimeUtil.daySdf));
		/*List<String> lines = readLines(new File("D:\\hhu.txt"));
		long ss = System.currentTimeMillis();
		int lineCount = 0;
		Set<HuInfo.HuType> huTypes = new HashSet<>();
		for(String str:lines){
			String[] hands = str.split(",");

			List<MJCard> mjCards = new LinkedList<>();
			for(String card:hands){
				if(card.trim().isEmpty()){
					continue;
				}
				MJCard mjCard=new MJCard();
				mjCard.setCardNum(Integer.valueOf(card));
				mjCards.add(mjCard);
			}

			PlayerCardInfo playerCardInfo = new PlayerCardInfo(mjCards,new ArrayList<CardGroup>(),mjCards.get(0).getCardNum());
			HuInfo infos = GhostMJHuUtils.checkHu(playerCardInfo);
			if(infos == null){
				System.out.println(str+"未胡");
			}else {
				//System.out.println(str+"胡");
				huTypes.add(infos.getHuType());
				lineCount++;
			}
		}

		System.out.println("胡牌count:"+lineCount+","+(System.currentTimeMillis()-ss)+"ms"+" ,"+Arrays.toString(huTypes.toArray()));*/
	}

	public static CardGroup createGroup(Integer... cards){
		LinkedList<Integer> MJCards = new LinkedList<>();
		Collections.addAll(MJCards,cards);
		CardGroup group = new CardGroup(0,MJCards);
		return group;
	}

	public static void  replace(int a,int b){
		System.out.println("a:"+a);
		System.out.println("b:"+b);
		a = a^b;
		b = a^b;
		a = a^b;
		System.out.println("change");
		System.out.println("a:"+a);
		System.out.println("b:"+b);
	}
}
