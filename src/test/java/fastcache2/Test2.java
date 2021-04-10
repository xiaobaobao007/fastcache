package fastcache2;

import java.awt.*;

import javax.swing.*;

import fastcache2.dao.GirlFriendDao;
import fastcache2.dao.PetDao;
import fastcache2.domain.file1.GirlFriend;
import fastcache2.domain.file2.Pet;
import pers.xiaobaobao.fastcache.factory.CacheFactory;
import pers.xiaobaobao.fastcache.factory.CglibProxyFactory;

public class Test2 {

	public static JTextField userIdText;
	public static JTextField idText;
	public static JTextField typeText;

	public static int FRAME_WIDTH = 400;
	public static int FRAME_HEIGHT = 500;

	public void init() {
		JFrame jFrame = new JFrame("测试系统");
		JPanel jPanel = new JPanel();
		Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int JFrame_X = (int) (ScreenSize.getWidth() - FRAME_WIDTH) / 2;
		int JFrame_Y = (int) (ScreenSize.getHeight() - FRAME_HEIGHT) / 2;
		jFrame.setLocation(JFrame_X, JFrame_Y);
		jFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		jFrame.setLayout(null);
		jPanel.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
		jPanel.setLayout(null);

		{
			int x = 120;
			int y = 20;
			int y_add = 45;

			JLabel jLabel1 = new JLabel("type");
			jLabel1.setFont(new Font("宋体", Font.BOLD, 15));
			jLabel1.setBounds(x - 50, y - 13, 200, 50);
			typeText = new JTextField("");
			typeText.setBounds(x + 30, y, 150, 30);
			jPanel.add(jLabel1);
			jPanel.add(typeText);

			y += y_add;
			JLabel jLabel2 = new JLabel("userId");
			jLabel2.setFont(new Font("宋体", Font.BOLD, 15));
			jLabel2.setBounds(x - 50, y - 13, 200, 50);
			userIdText = new JTextField("2");
			userIdText.setBounds(x + 30, y, 150, 30);
			jPanel.add(jLabel2);
			jPanel.add(userIdText);

			y += y_add;
			JLabel jLabel3 = new JLabel("id");
			jLabel3.setFont(new Font("宋体", Font.BOLD, 15));
			jLabel3.setBounds(x - 50, y - 13, 200, 50);
			idText = new JTextField("2");
			idText.setBounds(x + 30, y, 150, 30);
			jPanel.add(jLabel3);
			jPanel.add(idText);
		}
		/*
		 * list的测试开始
		 */
		JButton button = new JButton("getList");
		button.addActionListener((q) -> {
			for (int i = 0; i < 10; i++) {
				new Thread(() -> {
					System.out.println(PetDao.dao.getList(getUserId()));
				}).start();
			}
		});

		JButton button1 = new JButton("getOne");
		button1.addActionListener((q) -> {
			System.out.println(PetDao.dao.getOne(getUserId(), getId()));
		});

		JButton button2 = new JButton("updateOne");
		button2.addActionListener((q) -> {
			Pet pet = PetDao.dao.getOne(getUserId(), getId());
			if (pet != null) {
				pet.setNum(pet.getNum() + 1);
				PetDao.dao.update(pet);
			}
		});

		JButton button3 = new JButton("deleteOne");
		button3.addActionListener((q) -> {
			Pet pet = PetDao.dao.getOne(getUserId(), getId());
			PetDao.dao.delete(pet);
		});

		JButton button4 = new JButton("addOne");
		button4.addActionListener((q) -> {
			Pet pet = new Pet(getUserId(), (int) CacheFactory.getMaxId(Pet.class, getUserId()));
			PetDao.dao.save(pet);
		});

		{
			/*
			 one的测试开始
			 */
			JButton button5 = new JButton("getMaxId");
			button5.addActionListener((q) -> {
				System.out.println(CacheFactory.getMaxId(Pet.class, getUserId()));
			});

			JButton button6 = new JButton("getOne");
			button6.addActionListener((q) -> {
				for (int i = 0; i < 10; i++) {
					new Thread(() -> {
						System.out.println(GirlFriendDao.dao.get(getUserId()));
					}).start();
				}
			});

			JButton button7 = new JButton("updateOne");
			button7.addActionListener((q) -> {
				GirlFriend girlFriend = GirlFriendDao.dao.get(getUserId());
				if (girlFriend != null) {
					girlFriend.setAge(girlFriend.getAge() + 1);
					GirlFriendDao.dao.update(girlFriend);
				}
			});

			JButton button8 = new JButton("deleteOne");
			button8.addActionListener((q) -> {
				GirlFriend girlFriend = GirlFriendDao.dao.get(getUserId());
				GirlFriendDao.dao.delete(girlFriend);
			});

			JButton button9 = new JButton("addOne");
			button9.addActionListener((q) -> {
				GirlFriend girlFriend = new GirlFriend(getUserId());
				GirlFriendDao.dao.save(girlFriend);
			});

			int x = 30;
			int y = 155, i = y;
			int x_add = 170;
			int y_add = 60;

			button.setBounds(x, i, 145, 50);
			jPanel.add(button);
			button1.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button1);
			button2.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button2);
			button3.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button3);
			button4.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button4);
			button5.setBounds(x += x_add, i = y, 145, 50);
			jPanel.add(button5);
			button6.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button6);
			button7.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button7);
			button8.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button8);
			button9.setBounds(x, i += y_add, 145, 50);
			jPanel.add(button9);

			jFrame.add(jPanel);
			jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			jFrame.setVisible(true);
		}

	}

	public static void main(String[] args) {
		CglibProxyFactory.init("fastcache2.dao");
		new Test2().init();
	}

	public int getType() {
		return Integer.parseInt(typeText.getText());
	}

	public int getUserId() {
		return Integer.parseInt(userIdText.getText());
	}

	public int getId() {
		return Integer.parseInt(idText.getText());
	}

}