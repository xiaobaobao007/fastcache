import java.awt.*;
import java.util.Set;

import javax.swing.*;

import com.intion.fastcache.annotation.Cache;
import com.intion.fastcache.dao.ListDemoDao;
import com.intion.fastcache.factory.CglibProxyFactory;

import org.reflections.Reflections;

public class ATestService {

	public static JTextField userIdText;
	public static JTextField itemIdText;
	public static JTextField itemNumText;
	public static JTextField idText;
	public static JTextField typeText;
	public static JTextField paramText;
	public static JTextField param1Text;
	public static JTextField accountText;
	public static JTextField startText;
	public static JTextField endText;

	public static int JFRAME_WIDTH = 1220;
	public static int JFRAME_HEIGHT = 650;

	public void init() {
		JFrame jFrame = new JFrame("测试系统");
		JPanel jPanel = new JPanel();
		Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int JFrame_X = (int) (ScreenSize.getWidth() - JFRAME_WIDTH) / 2;
		int JFrame_Y = (int) (ScreenSize.getHeight() - JFRAME_HEIGHT) / 2;
		jFrame.setLocation(JFrame_X, JFrame_Y);
		jFrame.setSize(JFRAME_WIDTH, JFRAME_HEIGHT);
		jFrame.setLayout(null);
		jPanel.setBounds(0, 0, JFRAME_WIDTH, JFRAME_HEIGHT);
		jPanel.setLayout(null);

		int x = 60;
		int y = 20;

		JLabel jLabel1 = new JLabel("userId");
		jLabel1.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel1.setBounds(x - 50, y - 13, 200, 50);
		userIdText = new JTextField("1");
		userIdText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel1);
		jPanel.add(userIdText);

		x = 60;
		y = 80;
		JLabel jLabel2 = new JLabel("itemId");
		jLabel2.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel2.setBounds(x - 50, y - 13, 200, 50);
		itemIdText = new JTextField("");
		itemIdText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel2);
		jPanel.add(itemIdText);

		x = 60;
		y = 140;
		JLabel jLabel3 = new JLabel("itemNum");
		jLabel3.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel3.setBounds(x - 50, y - 13, 200, 50);
		itemNumText = new JTextField("");
		itemNumText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel3);
		jPanel.add(itemNumText);

		x = 60;
		y = 200;
		JLabel jLabel4 = new JLabel("id");
		jLabel4.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel4.setBounds(x - 50, y - 13, 200, 50);
		idText = new JTextField("2");
		idText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel4);
		jPanel.add(idText);

		x = 60;
		y = 260;
		JLabel jLabel5 = new JLabel("type");
		jLabel5.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel5.setBounds(x - 50, y - 13, 200, 50);
		typeText = new JTextField("");
		typeText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel5);
		jPanel.add(typeText);

		x = 250;
		y = 20;
		JLabel jLabel6 = new JLabel("param");
		jLabel6.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel6.setBounds(x - 50, y - 13, 200, 50);
		paramText = new JTextField("");
		paramText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel6);
		jPanel.add(paramText);

		x = 250;
		y = 80;
		JLabel jLabel7 = new JLabel("param1");
		jLabel7.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel7.setBounds(x - 50, y - 13, 200, 50);
		param1Text = new JTextField("");
		param1Text.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel7);
		jPanel.add(param1Text);

		x = 250;
		y = 140;
		JLabel jLabel8 = new JLabel("account");
		jLabel8.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel8.setBounds(x - 50, y - 13, 200, 50);
		accountText = new JTextField("");
		accountText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel8);
		jPanel.add(accountText);

		x = 250;
		y = 200;
		JLabel jLabel9 = new JLabel("start");
		jLabel9.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel9.setBounds(x - 50, y - 13, 200, 50);
		startText = new JTextField("");
		startText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel9);
		jPanel.add(startText);

		x = 250;
		y = 260;
		JLabel jLabel10 = new JLabel("end");
		jLabel10.setFont(new Font("宋体", Font.BOLD, 15));
		jLabel10.setBounds(x - 50, y - 13, 200, 50);
		endText = new JTextField("");
		endText.setBounds(x + 30, y, 90, 30);
		jPanel.add(jLabel10);
		jPanel.add(endText);

		JButton button = new JButton("getList");
		button.addActionListener((q) -> {
			System.out.println(ListDemoDao.dao.getList((int) getUserId()));
		});

		JButton button1 = new JButton("getOne");
		button1.addActionListener((q) -> {
			System.out.println(ListDemoDao.dao.getOne((int) getUserId(), (int) getId()));
		});

		JButton button2 = new JButton("");
		button2.addActionListener((q) -> {
		});

		JButton button3 = new JButton("");
		button3.addActionListener((q) -> {
		});

		JButton button4 = new JButton("");
		button4.addActionListener((q) -> {
		});

		JButton button5 = new JButton("");
		button5.addActionListener((q) -> {
		});

		JButton button6 = new JButton("");
		button6.addActionListener((q) -> {
		});

		JButton button7 = new JButton("");
		button7.addActionListener((q) -> {
		});

		JButton button8 = new JButton("");
		button8.addActionListener((q) -> {
		});

		JButton button9 = new JButton("");
		button9.addActionListener((q) -> {
		});

		JButton button_0 = new JButton("");
		button_0.addActionListener((q) -> {
		});

		JButton button_1 = new JButton("");
		button_1.addActionListener((q) -> {
		});

		JButton button_2 = new JButton("");
		button_2.addActionListener((q) -> {
		});

		JButton button_3 = new JButton("");
		button_3.addActionListener((q) -> {
		});

		JButton button_4 = new JButton("");
		button_4.addActionListener((q) -> {
		});

		JButton button_5 = new JButton("");
		button_5.addActionListener((q) -> {
		});

		{
			button.setBounds(50, 300, 145, 50);
			jPanel.add(button);
			button1.setBounds(50, 360, 145, 50);
			jPanel.add(button1);
			button2.setBounds(50, 420, 145, 50);
			jPanel.add(button2);
			button3.setBounds(50, 480, 145, 50);
			jPanel.add(button3);
			button4.setBounds(50, 540, 145, 50);
			jPanel.add(button4);
			button5.setBounds(220, 300, 145, 50);
			jPanel.add(button5);
			button6.setBounds(220, 360, 145, 50);
			jPanel.add(button6);
			button7.setBounds(220, 420, 145, 50);
			jPanel.add(button7);
			button8.setBounds(220, 480, 145, 50);
			jPanel.add(button8);
			button9.setBounds(220, 540, 145, 50);
			jPanel.add(button9);

			button_0.setBounds(1000, 300, 145, 50);
			jPanel.add(button_0);
			button_1.setBounds(1000, 360, 145, 50);
			jPanel.add(button_1);
			button_2.setBounds(1000, 420, 145, 50);
			jPanel.add(button_2);
			button_3.setBounds(1000, 480, 145, 50);
			jPanel.add(button_3);
			button_4.setBounds(1000, 540, 145, 50);
			jPanel.add(button_4);
			button_5.setBounds(830, 300, 145, 50);
			button_5.setBackground(Color.yellow);
			jPanel.add(button_5);

			jFrame.add(jPanel);
			jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			jFrame.setVisible(true);
		}

	}

	public static void main(String[] args) throws ClassNotFoundException {
		// new ATestService().init();

		CglibProxyFactory.init("com.intion.fastcache.dao");
	}

	public String getParam() {
		return paramText.getText();
	}

	public String getParam1() {
		return typeText.getText();
	}

	public long getUserId() {
		return Long.parseLong(userIdText.getText());
	}

	public long getItemId() {
		return Long.parseLong(itemIdText.getText());
	}

	public int getItemNum() {
		return Integer.parseInt(itemNumText.getText());
	}

	public long getId() {
		return Long.parseLong(idText.getText());
	}

	public int getType() {
		return Integer.parseInt(typeText.getText());
	}

	public int getParamToInt() {
		return Integer.parseInt(paramText.getText());
	}

	public int getParam1ToInt() {
		return Integer.parseInt(param1Text.getText());
	}

	public String getAccount() {
		return accountText.getText();
	}

	public int getStart() {
		return Integer.parseInt(startText.getText());
	}

	public int getEnd() {
		return Integer.parseInt(endText.getText());
	}

}
