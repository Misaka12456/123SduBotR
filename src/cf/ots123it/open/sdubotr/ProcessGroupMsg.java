package cf.ots123it.open.sdubotr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import cf.ots123it.jhlper.CommonHelper;
import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;

import static cf.ots123it.open.sdubotr.Global.*;
/**
 * 123 SduBotR 群聊消息处理类<br>
 * 注意:本类中任何方法前的CQ参数请在Start类中直接用CQ即可<br>
 * <i>（若在Start类的main测试方法中调用，请使用你所new的Start实例的getCoolQ方法<br>
 * 如: <pre class="code">ProcessGroupMsg.main(<b>demo.getCoolQ()</b>,123456789L,123456789L,"Hello world");</pre></i>）
 * @author 御坂12456
 */
@SuppressWarnings("deprecation")
public abstract class ProcessGroupMsg extends JcqAppAbstract
{

	/**
	 * 主调用处理方法
	 * @param CQ CQ实例，详见本类注释
	 * @param groupId 消息来源群号
	 * @param qqId 消息来源成员QQ号
	 * @param msg 消息内容
	 * @see ProcessGroupMsg
	 */
	public static void main(CoolQ CQ,long groupId,long qqId,String msg)
	{
		try {
	        // 读取特别监视群聊列表文件(功能2-1)
			File imMonitGroups = new File(Start.appDirectory + "/group/list/iMG.txt");
			if (imMonitGroups.exists()) { //如果列表文件存在
				for (String imMonitGroup : IOHelper.ReadAllLines(imMonitGroups)) {
					if (String.valueOf(groupId).equals(imMonitGroup)) //如果消息来源群为特别监视群
					{
						Part2.Func2_1(CQ,groupId,qqId,msg); //转到功能2-1处理
						break;
					}
				}
			}
			
		} catch (Exception e) {
			CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
		Part_Spec.Funny_EasterEgg(CQ, groupId, qqId, msg); //调用滑稽彩蛋方法
		if ((msg.startsWith("!")) ||  (msg.startsWith("！"))) // 如果消息开头是"!"或中文"！"
		{
			//去除指令前的"!"标记
			msg = msg.substring(1, msg.length()); 
			try {
				//获得所有参数组成的数组
				String[] arguments = msg.split(" ");
				//获得第一个参数
				String arg1 = arguments[0];
				switch (arg1) // //判断第一个参数
				{
				/* 主功能1:群管理核心功能 */
				case "mt": //功能1-1:禁言
					Part1.Func1_1(CQ, groupId, qqId, msg);
					break;
				case "um": //功能1-2:解禁
					Part1.Func1_2(CQ, groupId, qqId, msg);
					break;
				case "k": //功能1-3:踢人
					Part1.Func1_3(CQ, groupId, qqId, msg);
					break;
				case "fk": //功能1-4:永踢人（慎用）
					Part1.Func1_4(CQ, groupId, qqId, msg);
					break;
				/* 其它功能 */
				case "about": //功能O-1:关于
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					break;
				case "m": //功能O-2:功能菜单
					Part_Other.FuncO_Menu(CQ, groupId, qqId, msg);
					break;
				default:
					break;
				}
			}
			catch (NumberFormatException e) { //指令格式错误(1)
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
			catch (IndexOutOfBoundsException e) { //指令格式错误(2)
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
		}
	}
	/**
	 * 主功能1:群管理核心功能
	 * @author 御坂12456
	 *
	 */
	static class Part1{
		/**
		 * 功能1-1:禁言
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func1_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被禁言的成员QQ号或at）
				String arg3 = msg.split(" ", 3)[2]; //获取参数3（要禁言的时长(单位:分钟)）
				long muteQQ = 0, muteDuration = 0; //定义要被禁言的QQ号和禁言时长变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					muteQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						muteQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if ((CommonHelper.isInteger(arg3)) && (Integer.valueOf(arg3) >= 1) && (Integer.valueOf(arg3) <= 43200)) { //如果禁言时长为数字且范围在1-43200之间
					if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
						if(CQ.getGroupMemberInfo(groupId, muteQQ) != null) //如果对应成员在群内
						{
							if (!isGroupAdmin(CQ, groupId, muteQQ)) { //如果要被禁言的对象不是管理组成员
								if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
									if (muteQQ == CQ.getLoginQQ()) { // 如果试图禁言机器人QQ
										CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
												"我禁言我自己！？(402)");
									} else { //否则（被禁言对象不是机器人QQ）
										muteDuration = Long.parseLong(arg3); //将禁言时长字符串转成长整型
										CQ.setGroupBan(groupId, muteQQ,muteDuration * 60); //执行禁言操作
										CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
												"操作完成(200)");
									}
								} else { //否则（指令执行者不是管理组成员）
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"出现了！越权操作！(403)");
								}
							} else { //否则（被禁言的对象是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"本是同管理组人，相禁何太急orz(403)");
							}
						} else { //否则（对应成员不在群内）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"这人没在群里啊QwQ(302)");
						}

					} else { //否则（机器人不是管理组成员）
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
								"没这个权限QAQ(401)");
					}
				} else { //否则（禁言时长不是数字或范围不在1-43200内）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" +
							"您输入了无效的禁言时长,请更正后重试(301)\n" + 
							"时长范围(分钟): 1 - 43200\n" + 
							"常用禁言时长:\n" +
							"1小时-60 1天-1440\n" + 
							"30天-43200(QQ手机版极限:43199)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
							"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  
			catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-2:解禁
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func1_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被解禁的成员QQ号或at）
				long unMuteQQ = 0; //定义要被解禁的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					unMuteQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						unMuteQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, unMuteQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, unMuteQQ)) { //如果要被禁言的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (unMuteQQ == CQ.getLoginQQ()) { // 如果试图解禁机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"解禁我自己，这……(402)");
								} else { //否则（被解禁对象不是机器人QQ）
									CQ.setGroupBan(groupId, unMuteQQ,0); //执行解禁操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"操作完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被解禁的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"喂喂，要是管理被禁言就别找我了吧(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}

				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!um [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-3:踢人
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func1_3(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被踢的成员QQ号或at）
				long kickedQQ = 0; //定义要被踢的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					kickedQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						kickedQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, kickedQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, kickedQQ)) { //如果要被踢的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (kickedQQ == CQ.getLoginQQ()) { // 如果试图踢掉机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"要是看我不顺眼的话直接手动踢了我吧QAQ(402)");
								} else { //否则（被踢对象不是机器人QQ）
									CQ.setGroupKick(groupId, kickedQQ, false); //执行踢出操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"操作完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被踢的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"别踢管理组成员啊，都是一家人orz(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}
				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!k [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-4:永踢人（慎用）
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func1_4(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被踢的成员QQ号或at）
				long foreverKickedQQ = 0; //定义要被踢的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					foreverKickedQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						foreverKickedQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
								"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, foreverKickedQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, foreverKickedQQ)) { //如果要被踢的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (foreverKickedQQ == CQ.getLoginQQ()) { // 如果试图踢掉机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"求求不要永踢我！如果实在看不惯就直接手动踢了吧QAQ(402)");
								} else { //否则（被踢对象不是机器人QQ）
									CQ.setGroupKick(groupId, foreverKickedQQ, true); //执行永踢操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"操作完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被踢的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"别踢管理组成员啊，都是一家人orz(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}
				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!fk [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

	}

	/**
	 * 主功能2:群管理辅助功能
	 * @author 御坂12456
	 *
	 */
	static class Part2
	{
		/**
		 * 功能2-1:特别监视违禁词提醒功能
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456(优化: Sugar 404)
		 */
		public static void Func2_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			// 判断违禁词列表是否为空
			String iMGBanConfirm = IOHelper.ReadToEnd(Global.appDirectory + "/group/list/iMGBan.txt");
			if(iMGBanConfirm.equals(""))
			{
				return;
			} // 否则
			System.gc(); //通知Java进行垃圾收集
			String[] iMGBans = IOHelper.ReadAllLines(Global.appDirectory + "/group/list/iMGBan.txt");
			List<String> bans = new ArrayList<>();
			for (String iMGBanString : iMGBans) {
				if (msg.indexOf(iMGBanString) != -1) { // 若消息内容包含违禁词
					bans.add(iMGBanString);
				}
			}
			if (!bans.isEmpty()) {
				StringBuilder b = new StringBuilder();
				b.append(Global.FriendlyName).append("\n检测到有人发布违禁词，请尽快查看\n来源群号:")
				.append(Global.getGroupName(CQ, groupId)).append('(').append(groupId).append(")\n来源QQ:")
				.append(CQ.getGroupMemberInfo(groupId, qqId).getNick()).append('(').append(qqId)
				.append(")\n检测到的违禁词:");
				for (String iMGBanString:bans) {
					b.append(iMGBanString).append('.');
				}
				b.append("\n完整消息内容:\n").append(msg);
				CQ.sendPrivateMsg(Global.masterQQ,b.toString());
			}
			return;
		}

	}

	/**
	 * 其它功能（注意与"特殊模块"区分开）
	 * @author 御坂12456
	 *
	 */
	static class Part_Other{
		/**
		 * 功能O-1:关于123 SduBotR
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void FuncO_About(CoolQ CQ,long groupId,long qqId,String msg)
		{
			// 创建"关于"字符串生成器（可变字符串）对象
			StringBuilder aboutStrBuilder = new StringBuilder();
			/*
			 * "关于"内容:
			 * 123 SduBotR
			 * 一个完全开源的酷Q QQ bot程序
			 * 原作者:御坂12456(QQ:770296414)
			 * 官方群:812655602
			 * 当前登录账号:[昵称]([QQ])
			 * Github仓库:https://github.com/Misaka12456/123SduBotR
			 * 建议使用酷Q Pro运行本程序
			 */
			aboutStrBuilder.append("123 SduBotR\n")
			.append("一个完全开源的酷Q QQ bot程序\n")
			.append("原作者:御坂12456(QQ:770296414)\n")
			.append("官方群:812655602\n")
			.append("当前登录账号:").append(CQ.getLoginNick()).append("(").append(String.valueOf(CQ.getLoginQQ())).append(")\n")
			.append("Github仓库:https://github.com/Misaka12456/123SduBotR\n")
			.append("建议使用酷Q Pro运行本程序");
			// 发送消息
			CQ.sendGroupMsg(groupId, aboutStrBuilder.toString());
			return;
		}
		/**
		 * 功能O-2:功能菜单
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void FuncO_Menu(CoolQ CQ,long groupId,long qqId,String msg)
		{
			int result = CQ.sendPrivateMsg(qqId, menuStr); //私聊发送功能菜单
			switch (result) //判断功能菜单发送结果
			{
			case -36: //群主禁止临时会话
				CQ.sendGroupMsg(groupId,FriendlyName + "\n" + 
						"群主设置禁止临时会话了……去https://github.com/Misaka12456/123SduBotR/blob/master/README.md看菜单吧(501:-36)");
				break;
			case -35: //权限不足，可能解除了与对方的好友关系
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
						"bot被屏蔽了呀……怎么发功能菜单啊QAQ(500:-35)");
				break;
			case -30: //消息被服务器拒绝
				CQ.sendGroupMsg(groupId,FriendlyName + "\n" + 
						"TX拒绝了bot发送消息的请求……我也没办法啊(500:-30)");
				break;
			default: //其它情况
				if (String.valueOf(result).startsWith("-")) { //发送消息失败，未知原因
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"TX又炸了，功能发不出去orz(500:" + String.valueOf(result) + ")");
				} else {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
						"功能菜单已发送至私聊(若接收不到请尝试重新发送指令)");
				}
				break;
			}
			return;
		}
	}
	/**
	 * 特殊模块
	 * @author 御坂12456
	 *
	 */
	static class Part_Spec
	{
		/**
		 * 特殊功能:滑稽（斜眼笑）彩蛋
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		static void Funny_EasterEgg(CoolQ CQ,long groupId,long qqId,String msg)
		{
			//若滑稽彩蛋白名单文件存在
			if ((new File(Global.appDirectory + "/group/list/funnyWL.txt").exists()))
			{
				//读取滑稽彩蛋白名单群列表
				String[] funnyWhiteList = IOHelper.ReadAllLines(Global.appDirectory + "/group/list/funnyWL.txt");
				//若白名单列表不为空
				if (funnyWhiteList != null) {
					for (String funnyWhiteGroup : funnyWhiteList) {
						if (String.valueOf(groupId).equals(funnyWhiteGroup)) //若此群为白名单群
						{
							CQ.logDebug(Global.AppName, "当前群聊:" + groupId + "属于滑稽彩蛋白名单群聊,已跳过处理");
							return; //不执行后续代码，直接返回
						}
					}
				}
			}
			String[] funnyStrings = {"[CQ:face,id=178]", //滑稽
					"[CQ:face,id=178][CQ:emoji,id=127166]", //滑稽+水滴
					"[CQ:face,id=178][CQ:face,id=66]", //滑稽+爱心
					"[CQ:face,id=178][CQ:face,id=147]", //滑稽+棒棒糖
					"[CQ:face,id=178][CQ:emoji,id=10068]", //滑稽+问号
					"[CQ:face,id=178][CQ:emoji,id=10069]", //滑稽+叹号
					"[CQ:face,id=178][CQ:face,id=67]" //滑稽+心碎
			};
			// 获取一个0到1000的整数并存储到变量i中
			int i = ThreadLocalRandom.current().nextInt(1000);
			// 获取一个0到1000的整数并存储到变量j中
			int j = ThreadLocalRandom.current().nextInt(1000);
			// 将j与i相减，赋值给k
			int k = j - i;
			if (k < -997) //如果k小于997
			{
				CQ.sendGroupMsg(groupId, funnyStrings[0]); //发送滑稽数组第1个消息
			} else if ((k >= -502) & (k <= -500)) //如果k在-500~-502
			{
				CQ.sendGroupMsg(groupId, funnyStrings[1]); //发送滑稽数组第2个消息
			} else if ((k >= -241) & (k <= -239))  //如果k在-239~-241
			{
				CQ.sendGroupMsg(groupId, funnyStrings[2]);  //发送滑稽数组第3个消息
			} else if ((k >= -1) & (k <= 1)) //如果k在-1~1
			{
				CQ.sendGroupMsg(groupId, funnyStrings[3]);  //发送滑稽数组第4个消息
			} else if ((k >= 299) & (k <= 301)) //如果k在299~301
			{
				CQ.sendGroupMsg(groupId, funnyStrings[4]);  //发送滑稽数组第5个消息
			} else if ((k >= 499) & (k <= 501)) //如果k在499~501
			{
				CQ.sendGroupMsg(groupId, funnyStrings[5]);  //发送滑稽数组第6个消息
			} else if ((k > 997)) //如果k大于997
			{
				CQ.sendGroupMsg(groupId, funnyStrings[6]);  //发送滑稽数组第7个消息
			}
			System.gc(); //执行垃圾收集器
			return;
		}
	}
}
