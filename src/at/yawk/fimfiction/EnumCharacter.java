package at.yawk.fimfiction;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

public enum EnumCharacter {
	TWILIGHT_SPARKLE(7, "Twilight Sparkle", "http://www.fimfiction-static.net/images/characters/twilight_sparkle.png"),
	RAINBOW_DASH(8, "Rainbow Dash", "http://www.fimfiction-static.net/images/characters/rainbow_dash.png"),
	PINKIE_PIE(9, "Pinkie Pie", "http://www.fimfiction-static.net/images/characters/pinkie_pie.png"),
	APPLEJACK(10, "Applejack", "http://www.fimfiction-static.net/images/characters/applejack.png"),
	RARITY(11, "Rarity", "http://www.fimfiction-static.net/images/characters/rarity.png"),
	FLUTTERSHY(12, "Fluttershy", "http://www.fimfiction-static.net/images/characters/fluttershy.png"),
	SPIKE(16, "Spike", "http://www.fimfiction-static.net/images/characters/spike.png"),
	MAIN_6(74, "Main 6", "http://www.fimfiction-static.net/images/characters/main_6.png", 64),
	APPLE_BLOOM(13, "Apple Bloom", "http://www.fimfiction-static.net/images/characters/apple_bloom.png"),
	SCOOTALOO(14, "Scootaloo", "http://www.fimfiction-static.net/images/characters/scootaloo.png"),
	SWEETIE_BELLE(15, "Sweetie Belle", "http://www.fimfiction-static.net/images/characters/sweetie_belle.png"),
	CUTIE_MARK_CRUSADERS(75, "Cutie Mark Crusaders", "http://www.fimfiction-static.net/images/characters/cmc.png", 64),
	BABS_SEED(84, "Babs Seed", "http://www.www.fimfiction-static.net/images/characters/babs_seed.png"),
	PRINCESS_CELESTIA(17, "Princess Celestia", "http://www.fimfiction-static.net/images/characters/celestia.png"),
	PRINCESS_LUNA(18, "Princess Luna", "http://www.fimfiction-static.net/images/characters/princess_luna.png"),
	NIGHTMARE_MOON(54, "Nightmare Moon", "http://www.fimfiction-static.net/images/characters/nightmare_moon.png"),
	GILDA(19, "Gilda", "http://www.fimfiction-static.net/images/characters/gilda.png"),
	ZECORA(20, "Zecora", "http://www.fimfiction-static.net/images/characters/zecora.png"),
	TRIXIE(21, "Trixie", "http://www.fimfiction-static.net/images/characters/trixie.png"),
	CHERILEE(30, "Cherilee", "http://www.fimfiction-static.net/images/characters/cherilee.png"),
	THE_MAYOR(31, "The Mayor", "http://www.fimfiction-static.net/images/characters/the_mayor.png"),
	HOITY_TOITY(32, "Hoity Toity", "http://www.fimfiction-static.net/images/characters/hoity_toity.png"),
	PHOTO_FINISH(33, "Photo Finish", "http://www.fimfiction-static.net/images/characters/photo_finish.png"),
	SAPPHIRE_SHORES(34, "Sapphire Shores", "http://www.fimfiction-static.net/images/characters/sapphire_shores.png"),
	SPITFIRE(35, "Spitfire", "http://www.fimfiction-static.net/images/characters/spitfire.png"),
	SOARIN(36, "Soarin", "http://www.fimfiction-static.net/images/characters/soarin.png"),
	PRINCE_BLUEBLOOD(37, "Prince Blueblood", "http://www.fimfiction-static.net/images/characters/prince_blueblood.png"),
	LITTLE_STRONGHEART(38, "Little Strongheart", "http://www.fimfiction-static.net/images/characters/little_strongheart.png"),
	DISCORD(53, "Discord", "http://www.fimfiction-static.net/images/characters/discord.png"),
	MARE_DO_WELL(58, "Mare Do Well", "http://www.fimfiction-static.net/images/characters/mare_do_well.png"),
	FANCYPANTS(60, "Fancypants", "http://www.fimfiction-static.net/images/characters/fancypants.png"),
	DARING_DO(63, "Daring Do", "http://www.fimfiction-static.net/images/characters/daring_do.png"),
	FLIM_AND_FLAM(65, "Flim and Flam", "http://www.fimfiction-static.net/images/characters/flimflamicon.png"),
	CRANKY_DOODLE_DONKEY(66, "Cranky Doodle Donkey", "http://www.fimfiction-static.net/images/characters/cranky doodle icon.png"),
	MATILDA(67, "Matilda", "http://www.fimfiction-static.net/images/characters/matilda icon.png"),
	MR_CAKE(68, "Mr. Cake", "http://www.fimfiction-static.net/images/characters/mr cake icon.png"),
	MRS_CAKE(69, "Mrs. Cake", "http://www.fimfiction-static.net/images/characters/mrs cake icon.png"),
	IRON_WILL(71, "Iron Will", "http://www.fimfiction-static.net/images/characters/ironwillicon.png"),
	PRINCESS_CADENCE(72, "Princess Cadence", "http://www.fimfiction-static.net/images/characters/Cadence.png"),
	SHINING_ARMOR(73, "Shining Armor", "http://www.fimfiction-static.net/images/characters/shining-armor.png"),
	WONDERBOLTS(76, "Wonderbolts", "http://www.fimfiction-static.net/images/characters/wonderbolts.png", 64),
	DIAMOND_DOGS(77, "Diamond Dogs", "http://www.fimfiction-static.net/images/characters/diamond_dogs.png", 64),
	QUEEN_CHRYSALIS(78, "Queen Chrysalis", "http://www.fimfiction-static.net/images/characters/queen-chrysalis.png"),
	KING_SOMBRA(83, "King Sombra", "http://www.fimfiction-static.net/images/characters/king-sombra.png"),
	BIG_MACINTOSH(22, "Big Macintosh", "http://www.fimfiction-static.net/images/characters/big_mac.png"),
	GRANNY_SMITH(23, "Granny Smith", "http://www.fimfiction-static.net/images/characters/granny_smith.png"),
	BRAEBURN(24, "Braeburn", "http://www.fimfiction-static.net/images/characters/braeburn.png"),
	DIAMOND_TIARA(25, "Diamond Tiara", "http://www.fimfiction-static.net/images/characters/diamond_tiara.png"),
	SILVER_SPOON(26, "Silver Spoon", "http://www.fimfiction-static.net/images/characters/silver_spoon.png"),
	TWIST(27, "Twist", "http://www.fimfiction-static.net/images/characters/twist.png"),
	SNIPS(28, "Snips", "http://www.fimfiction-static.net/images/characters/snips.png"),
	SNAILS(29, "Snails", "http://www.fimfiction-static.net/images/characters/snails.png"),
	PIPSQUEAK(55, "Pipsqueak", "http://www.fimfiction-static.net/images/characters/pipsqueak.png"),
	ANGEL(39, "Angel", "http://www.fimfiction-static.net/images/characters/angel.png"),
	WINONA(40, "Winona", "http://www.fimfiction-static.net/images/characters/winona.png"),
	OPALESCENCE(41, "Opalescence", "http://www.fimfiction-static.net/images/characters/opalescence.png"),
	GUMMY(42, "Gummy", "http://www.fimfiction-static.net/images/characters/gummy.png"),
	OWLOWISCIOUS(43, "Owlowiscious", "http://www.fimfiction-static.net/images/characters/owlowiscious.png"),
	PHILOMENA(44, "Philomena", "http://www.fimfiction-static.net/images/characters/philomena.png"),
	TANK(59, "Tank", "http://www.fimfiction-static.net/images/characters/tank.png"),
	DERPY_HOOVES(45, "Derpy Hooves", "http://www.fimfiction-static.net/images/characters/derpy_hooves.png"),
	LYRA(46, "Lyra", "http://www.fimfiction-static.net/images/characters/lyra.png"),
	BONBON(47, "Bon-Bon", "http://www.fimfiction-static.net/images/characters/bon_bon.png"),
	DJ_P0N3(48, "DJ P0N-3", "http://www.fimfiction-static.net/images/characters/dj_pon3.png"),
	CARAMEL(50, "Caramel", "http://www.fimfiction-static.net/images/characters/caramel.png"),
	DOCTOR_WHOOVES(51, "Doctor Whooves", "http://www.fimfiction-static.net/images/characters/doctor_whooves.png"),
	OCTAVIA(52, "Octavia", "http://www.fimfiction-static.net/images/characters/octavia.png"),
	BERRY_PUNCH(56, "Berry Punch", "http://www.fimfiction-static.net/images/characters/berry_punch.png"),
	CARROT_TOP(57, "Carrot Top", "http://www.fimfiction-static.net/images/characters/carrot_top.png"),
	FLEUR_DE_LIS(61, "Fleur de Lis", "http://www.fimfiction-static.net/images/characters/fleur_de_lis.png"),
	COLGATE(64, "Colgate", "http://www.fimfiction-static.net/images/characters/colgateicon.png"),
	DINKY_HOOVES(70, "Dinky Hooves", "http://www.fimfiction-static.net/images/characters/dinkyicon.png"),
	THUNDERLANE(79, "Thunderlane", "http://www.fimfiction-static.net/images/characters/thunderlane.png"),
	FLITTER_AND_CLOUDCHASER(80, "Flitter and Cloudchaser", "http://www.fimfiction-static.net/images/characters/flitter_and_cloudchaser.png"),
	RUMBLE(81, "Rumble", "http://www.fimfiction-static.net/images/characters/rumble.png"),
	ROSELUCK(82, "Roseluck", "http://www.fimfiction-static.net/images/characters/roseluck.png"),
	CHANGELINGS(85, "Changelings", "http://www.www.fimfiction-static.net/images/characters/changelings.png"),
	ORIGINAL_CHARACTER(49, "Original Character", "http://www.fimfiction-static.net/images/characters/oc.png"),
	OTHER(62, "Other", "http://www.fimfiction-static.net/images/characters/other.png");
	
	private final static Executor	imageDownloader	= Executors.newFixedThreadPool(5);
	
	private final int				id;
	private final String			displayName;
	private final String			imageUrl;
	private BufferedImage			image			= null;
	private boolean					isLoadingImage	= false;
	private final Set<Runnable>		onFinishLoad	= new HashSet<Runnable>();
	private final int				imageWidth;
	
	private EnumCharacter(final int id, final String displayName, final String imageUrl) {
		this(id, displayName, imageUrl, 32);
	}
	
	private EnumCharacter(final int id, final String displayName, final String imageUrl, final int imageWidth) {
		this.id = id;
		this.displayName = displayName;
		this.imageUrl = imageUrl;
		this.imageWidth = imageWidth;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public BufferedImage getImage() {
		if(image != null) {
			return image;
		} else if(isLoadingImage) {
			return null;
		} else {
			imageDownloader.execute(new Runnable() {
				@Override
				public void run() {
					try {
						image = ImageIO.read(webProvider.getConnection(new URL(imageUrl)).getInputStream());
						final Iterator<Runnable> i = onFinishLoad.iterator();
						while(i.hasNext()) {
							final Runnable r = i.next();
							r.run();
							i.remove();
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			});
			return null;
		}
	}
	
	public void addOnLoad(Runnable r) {
		if(image == null)
			onFinishLoad.add(r);
		else
			r.run();
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	private static IWebProvider	webProvider	= new StandardInternetProvider();
	
	public static void setSpecialWebProvider(final IWebProvider web) {
		webProvider = web;
	}
	
	public static EnumCharacter parse(String s) {
		return names.get(s.toUpperCase());
	}
	
	public static EnumCharacter parseImageUrl(String s) {
		return imageurls.get(s);
	}
	
	private static final Map<String, EnumCharacter>	names		= new HashMap<String, EnumCharacter>(values().length);
	private static final Map<String, EnumCharacter>	imageurls	= new HashMap<String, EnumCharacter>(values().length);
	
	static {
		for(EnumCharacter e : values()) {
			names.put(e.toString(), e);
			imageurls.put(e.getImageUrl(), e);
		}
	}
}
