package module.siadap.domain;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import myorg.domain.User;
import myorg.domain.scheduler.ReadCustomTask;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.fenixWebFramework.services.ServiceManager;
import pt.ist.fenixWebFramework.services.ServicePredicate;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class ImportSiadapStructure extends ReadCustomTask {

    Map<Integer, List<Evaluator>> avaliators = new HashMap<Integer, List<Evaluator>>();
    List<Responsible> responsibles = new ArrayList<Responsible>();
    public static Map<String, String> unknownUsersMap = new HashMap<String, String>();

    static {
	unknownUsersMap.put("ist25068", "Pedro Miguel Simões Coito");
	unknownUsersMap.put("ist25072", "Alvarinho Carvalho do Espirito Santo");
	unknownUsersMap.put("ist25079", "Adolfo Pereira Moura");
	unknownUsersMap.put("ist25095", "Marcelo Gurgel Figueiredo Moleiro");
	unknownUsersMap.put("ist25096", "Rute Catarina Panaças Guerreiro");
	unknownUsersMap.put("ist25104", "Teresa Jacinto de Oliveira Marques");
	unknownUsersMap.put("ist25106", "Miriam Mano Ferreira");
	unknownUsersMap.put("ist25109", "Elisabete Moreira de Oliveira Pino");
	unknownUsersMap.put("ist25110", "Dino Rodrigues Pereira das Neves");
	unknownUsersMap.put("ist25111", "Fábio André Duarte Morgado");
	unknownUsersMap.put("ist25112", "Hugo Alexandre Gonçalves Furtado");
	unknownUsersMap.put("ist25115", "Sandra Nazaré Gomes da Fonseca");
	unknownUsersMap.put("ist25118", "Joana Alves Lindinho Nunes de Castro");
	unknownUsersMap.put("ist25121", "Aurora Bonfim de Carvalho Oliveira");
	unknownUsersMap.put("ist25125", "Tiago Luís Ramos Silva Machado");
    }
    
    private final static String csvContent = new String("6100,ist24235,ist12282,\n"+
    		"6100,ist24728,ist24235,\n"+
    		"6211,ist23674,ist24235,\n"+
    		"6211,ist23020,ist23674,\n"+
    		"6213,ist23223,ist23674,\n"+
    		"6213,ist22669,ist23223,\n"+
    		"6213,ist22998,ist23223,\n"+
    		"6213,ist24099,ist23223,\n"+
    		"6213,ist24187,ist23223,\n"+
    		"6213,ist24292,ist23223,\n"+
    		"6213,ist10482803,ist23223,\n"+
    		"6213,ist24753,ist23223,\n"+
    		"6213,ist24975,ist23223,1\n"+
    		"6212,ist22337,ist23674,\n"+
    		"6212,ist21902,ist22337,\n"+
    		"6212,ist22643,ist22337,\n"+
    		"6212,ist23474,ist22337,\n"+
    		"6212,ist24061,ist22337,\n"+
    		"6212,ist10482794,ist22337,1\n"+
    		"6212,ist24454,ist22337,\n"+
    		"6215,ist24661,ist23674,\n"+
    		"6215,ist22352,ist24661,\n"+
    		"6215,ist23343,ist24661,\n"+
    		"6215,ist23558,ist24661,\n"+
    		"6215,ist24075,ist24661,\n"+
    		"6215,ist24129,ist24661,\n"+
    		"6215,ist24270,ist24661,1\n"+
    		"6215,ist24349,ist24661,\n"+
    		"6215,ist24379,ist24661,1\n"+
    		"6215,ist24641,ist24661,\n"+
    		"6215,ist24676,ist24661,\n"+
    		"6215,ist24988,ist24661,1\n"+
    		"6215,ist24989,ist24661,1\n"+
    		"6221,ist23461,ist24235,\n"+
    		"6221,ist22328,ist23461,\n"+
    		"6221,ist23063,ist23461,\n"+
    		"6223,ist22233,ist23461,\n"+
    		"6223,ist22510,ist23461,\n"+
    		"6223,ist22969,ist23461,\n"+
    		"6223,ist23475,ist23461,\n"+
    		"6223,ist23916,ist23461,\n"+
    		"6223,ist90111,ist23461,1\n"+
    		"6224,ist21903,ist23461,\n"+
    		"6224,ist22349,ist21903,\n"+
    		"6224,ist22520,ist21903,\n"+
    		"6224,ist22717,ist21903,\n"+
    		"6224,ist22755,ist21903,\n"+
    		"6224,ist22798,ist21903,\n"+
    		"6224,ist23620,ist21903,\n"+
    		"6224,ist23623,ist21903,\n"+
    		"6224,ist23667,ist21903,\n"+
    		"6224,ist23752,ist21903,\n"+
    		"6224,ist23756,ist21903,\n"+
    		"6224,ist23830,ist21903,\n"+
    		"6224,ist24120,ist21903,1\n"+
    		"6224,ist24305,ist21903,\n"+
    		"6224,ist24450,ist21903,\n"+
    		"6224,ist24663,ist21903,\n"+
    		"6224,ist24884,ist21903,\n"+
    		"6224,ist24964,ist21903,1\n"+
    		"6224,ist24965,ist21903,1\n"+
    		"6224,ist24966,ist21903,1\n"+
    		"6224,ist24985,ist21903,1\n"+
    		"6242,ist23739,ist24588,\n"+
    		"6242,ist24055,ist23739,\n"+
    		"6242,ist24512,ist23739,\n"+
    		"6242,ist24541,ist23739,\n"+
    		"6242,ist24741,ist23739,\n"+
    		"6243,ist24062,ist24588,\n"+
    		"6243,ist22997,ist24062,\n"+
    		"6243,ist23052,ist24062,\n"+
    		"6243,ist24053,ist24062,\n"+
    		"6243,ist24389,ist24062,1\n"+
    		"6243,ist24532,ist24062,\n"+
    		"6243,ist24980,ist24062,1\n"+
    		"6244,ist24588,,\n"+
    		"6244,ist23248,ist24588,\n"+
    		"6244,ist23409,ist24588,\n"+
    		"6244,ist23465,ist24588,\n"+
    		"6244,ist23484,ist24588,\n"+
    		"6244,ist23737,ist24588,\n"+
    		"6244,ist24056,ist24588,\n"+
    		"6244,ist24059,ist24588,\n"+
    		"6244,ist24645,ist24588,\n"+
    		"6244,ist24700,ist24588,\n"+
    		"6244,ist32393,ist24588,\n"+
    		"6244,ist24960,ist24588,1\n"+
    		"6244,ist24961,ist24588,1\n"+
    		"6244,ist24962,ist24588,1\n"+
    		"6301,ist23470,ist24235,\n"+
    		"6301,ist21493,ist22990,\n"+
    		"6301,ist22800,ist22990,\n"+
    		"6301,ist22990,ist23470,\n"+
    		"6301,ist23560,ist23470,\n"+
    		"6301,ist23638,ist22990,\n"+
    		"6301,ist24979,ist23470,1\n"+
    		"6301,ist24981,ist23470,1\n"+
    		"6311,ist22757,ist23470,\n"+
    		"6315,ist10482756,ist22757,1\n"+
    		"6315,ist21468,ist22757,\n"+
    		"6315,ist21488,ist22757,\n"+
    		"6315,ist22483,ist22757,\n"+
    		"6315,ist22506,ist22757,\n"+
    		"6315,ist22672,ist22757,\n"+
    		"6315,ist22680,ist22757,\n"+
    		"6315,ist23011,ist22757,\n"+
    		"6315,ist23018,ist22757,\n"+
    		"6315,ist23048,ist22757,\n"+
    		"6315,ist23867,ist22757,\n"+
    		"6315,ist24394,ist22757,\n"+
    		"6315,ist25021,ist22757,\n"+
    		"6315,ist25068,ist22757,1\n"+
    		"6312,ist22234,ist22757,\n"+
    		"6312,ist24225,ist22234,\n"+
    		"6312,ist24229,ist22234,\n"+
    		"6312,ist24720,ist22234,1\n"+
    		"6313,ist23555,ist22757,\n"+
    		"6313,ist23032,ist23555,\n"+
    		"6313,ist23073,ist23555,\n"+
    		"6313,ist23478,ist23555,\n"+
    		"6313,ist23804,ist23555,\n"+
    		"6313,ist24935,ist23555,\n"+
    		"6314,ist21692,ist22757,\n"+
    		"6314,ist22685,ist22757,\n"+
    		"6314,ist23306,ist22757,\n"+
    		"6316,ist23711,ist22757,\n"+
    		"6316,ist21496,ist23711,\n"+
    		"6316,ist22708,ist23711,1\n"+
    		"6316,ist24508,ist23711,1\n"+
    		"6316,ist24509,ist23711,1\n"+
    		"6316,ist24613,ist23711,1\n"+
    		"6316,ist24614,ist23711,1\n"+
    		"6316,ist24615,ist23711,1\n"+
    		"6316,ist24755,ist23711,1\n"+
    		"6316,ist24894,ist23711,1\n"+
    		"6316,ist25121,ist23711,1\n"+
    		"6331,ist23470,ist24235,\n"+
    		"6332,ist10482686,ist23470,1\n"+
    		"6332,ist22500,ist23470,\n"+
    		"6332,ist22519,ist23470,\n"+
    		"6332,ist22773,ist23470,\n"+
    		"6332,ist22824,ist23470,\n"+
    		"6332,ist22967,ist23470,\n"+
    		"6332,ist23045,ist23470,\n"+
    		"6332,ist23559,ist23470,\n"+
    		"6332,ist23735,ist23470,\n"+
    		"6332,ist24321,ist23470,1\n"+
    		"6332,ist25037,ist23470,1\n"+
    		"6332,ist25038,ist23470,1\n"+
    		"6332,ist25041,ist23470,1\n"+
    		"6332,ist25072,ist23470,1\n"+
    		"6332,ist25109,ist23470,1\n"+
    		"6333,ist23057,ist23470,\n"+
    		"6333,ist20828,ist23057,\n"+
    		"6333,ist22521,ist23057,\n"+
    		"6333,ist22967,ist23057,\n"+
    		"6333,ist23466,ist23057,\n"+
    		"6333,ist23570,ist23057,\n"+
    		"6333,ist24685,ist23057,1\n"+
    		"6333,ist137325,ist23057,1\n"+
    		"6333,ist144371,ist23057,1\n"+
    		"6333,ist25024,ist23057,1\n"+
    		"6333,ist25039,ist23057,1\n"+
    		"6333,ist25055,ist23057,1\n"+
    		"6333,ist25079,ist23057,1\n"+
    		"6333,ist25095,ist23057,1\n"+
    		"6333,ist25110,ist23057,1\n"+
    		"6333,ist25112,ist23057,1\n"+
    		"6334,ist45951,,1\n"+
    		"6334,ist22492,ist23470,\n"+
    		"6334,ist23828,ist23470,\n"+
    		"6334,ist24999,ist23470,1\n"+
    		"6334,ist45951,ist23470,1\n"+
    		"6401,ist24877,ist24235,\n"+
    		"6401,ist22137,ist24877,\n"+
    		"6401,ist20831,ist22137,\n"+
    		"6401,ist22674,ist24877,\n"+
    		"6401,ist23930,ist24877,\n"+
    		"6401,ist24875,ist21846,1\n"+
    		"6401,ist25096,ist24877,1\n"+
    		"6401,ist21769,ist22137,\n"+
    		"6401,ist22686,ist21769,\n"+
    		"6401,ist22758,ist21769,\n"+
    		"6401,ist24385,ist21769,\n"+
    		"6401,ist24769,ist21769,\n"+
    		"6401,ist24888,ist21769,\n"+
    		"6401,ist32949,ist21769,1\n"+
    		"6412,ist22752,ist22137,\n"+
    		"6412,ist22329,ist22752,\n"+
    		"6412,ist23202,ist22752,\n"+
    		"6412,ist24064,ist22752,1\n"+
    		"6412,ist24252,ist22752,\n"+
    		"6412,ist21303,ist22137,\n"+
    		"6412,ist20940,ist21303,\n"+
    		"6412,ist21768,ist21303,\n"+
    		"6412,ist22751,ist21303,\n"+
    		"6412,ist23647,ist21303,\n"+
    		"6412,ist23889,ist21303,\n"+
    		"6412,ist24123,ist21303,\n"+
    		"6412,ist24688,ist21303,\n"+
    		"6412,ist24726,ist21303,\n"+
    		"6412,ist24954,ist21303,\n"+
    		"8401,ist12048,,\n"+
    		"8401,ist21505,ist12048,\n"+
    		"8401,ist21774,ist13499,\n"+
    		"8401,ist21846,ist24877,\n"+
    		"8401,ist21901,ist20225,\n"+
    		"8401,ist22154,ist12048,\n"+
    		"8401,ist22159,ist12048,\n"+
    		"8401,ist22240,ist20225,\n"+
    		"8401,ist22245,ist20225,\n"+
    		"8401,ist22619,ist13499,\n"+
    		"8401,ist22665,ist20225,\n"+
    		"8401,ist22862,ist12048,\n"+
    		"8401,ist20225,ist12048,\n"+
    		"8401,ist23000,ist12048,\n"+
    		"8401,ist23021,ist21846,\n"+
    		"8401,ist23487,ist21846,\n"+
    		"8401,ist23883,ist12048,\n"+
    		"8401,ist24421,ist13499,\n"+
    		"8401,ist24439,ist12048,\n"+
    		"8401,ist24506,ist12048,\n"+
    		"8401,ist24518,ist13499,\n"+
    		"8401,ist149676,ist12048,\n"+
    		"8401,ist24616,ist12048,1\n"+
    		"8401,ist148357,ist12048,\n"+
    		"8401,ist151339,ist12048,\n"+
    		"8401,ist24698,ist12048,1\n"+
    		"8401,ist146506,ist13499,\n"+
    		"8401,ist138407,ist12048,\n"+
    		"8401,ist149608,ist12048,1\n"+
    		"8401,ist152304,ist12048,1\n"+
    		"8401,ist153832,ist12048,1\n"+
    		"8401,ist46082,ist12048,1\n"+
    		"8422,ist21534,ist12048,\n"+
    		"8422,ist23704,ist21534,\n"+
    		"8411,ist12048,,\n"+
    		"8411,ist24459,ist12048,\n"+
    		"8411,ist22207,ist12048,\n"+
    		"8411,ist22988,ist12048,\n"+
    		"8411,ist23490,ist12048,1\n"+
    		"8411,ist23648,ist12048,\n"+
    		"8411,ist24462,ist12048,\n"+
    		"8411,ist143645,ist12048,1\n"+
    		"8411,ist24717,ist12048,\n"+
    		"8411,ist24727,ist12048,1\n"+
    		"8411,ist24869,ist12048,1\n"+
    		"8411,ist24983,ist12048,1\n"+
    		"8411,ist24984,ist12048,1\n"+
    		"8411,ist24908,ist12048,1\n"+
    		"8411,ist25125,ist12048,1\n"+
    		"8311,ist23978,ist13267,\n"+
    		"8311,ist21268,ist23978,\n"+
    		"8311,ist21272,ist23978,\n"+
    		"8311,ist22978,ist23978,\n"+
    		"8311,ist23919,ist23978,\n"+
    		"8311,ist24065,ist23978,\n"+
    		"8311,ist24280,ist23978,1\n"+
    		"8311,ist24293,ist23978,1\n"+
    		"8311,ist24329,ist23978,\n"+
    		"8311,ist24882,ist23978,\n"+
    		"8311,ist24896,ist23978,\n"+
    		"8312,ist23703,ist13267,\n"+
    		"8312,ist22142,ist23703,\n"+
    		"8312,ist22973,ist23703,\n"+
    		"8312,ist23003,ist23703,\n"+
    		"8312,ist23028,ist23703,\n"+
    		"8312,ist23068,ist23703,\n"+
    		"8312,ist23700,ist23703,\n"+
    		"8312,ist23720,ist23703,\n"+
    		"8312,ist23860,ist23703,\n"+
    		"8312,ist23861,ist23703,\n"+
    		"8312,ist23890,ist23703,\n"+
    		"8312,ist24230,ist23703,\n"+
    		"8312,ist24396,ist23703,\n"+
    		"8312,ist24545,ist23703,\n"+
    		"8312,ist24840,ist23703,\n"+
    		"8312,ist24883,ist23703,1\n"+
    		"8313,ist24412,ist13267,\n"+
    		"8313,ist22330,ist24412,\n"+
    		"8313,ist22498,ist24412,\n"+
    		"8313,ist22659,ist24412,\n"+
    		"8313,ist22975,ist24412,\n"+
    		"8313,ist22986,ist24412,\n"+
    		"8313,ist24414,ist24412,\n"+
    		"8314,ist23718,ist13267,\n"+
    		"8314,ist24195,ist23718,\n"+
    		"8314,ist90146,ist23718,1\n"+
    		"8314,ist90138,ist23718,1\n"+
    		"8210,ist12760,,\n"+
    		"8211,ist22592,ist12760,\n"+
    		"8211,ist23811,ist22592,\n"+
    		"8211,ist23857,ist22592,\n"+
    		"8211,ist23832,ist22592,\n"+
    		"8211,ist23472,ist22592,\n"+
    		"8211,ist32181,ist22592,\n"+
    		"8212,ist22592,,\n"+
    		"8212,ist23880,ist12760,\n"+
    		"8212,ist30615,ist12760,1\n"+
    		"8610,ist12451,,\n"+
    		"8610,ist23012,ist12451,\n"+
    		"8611,ist22230,ist12451,\n"+
    		"8611,ist23014,ist12451,\n"+
    		"8611,ist23069,ist12451,\n"+
    		"8611,ist23572,ist12451,\n"+
    		"8611,ist23848,ist12451,\n"+
    		"8611,ist24298,ist12451,1\n"+
    		"8611,ist24313,ist12451,\n"+
    		"8611,ist90394,ist12451,1\n"+
    		"8611,ist25048,ist12451,1\n"+
    		"8612,ist23918,ist12451,\n"+
    		"8612,ist23716,ist23918,\n"+
    		"8612,ist24044,ist23918,\n"+
    		"8612,ist24054,ist23918,\n"+
    		"8612,ist24247,ist23918,\n"+
    		"8613,ist23874,ist12451,\n"+
    		"8613,ist23008,ist23874,\n"+
    		"8613,ist23462,ist23874,\n"+
    		"8510,ist12316,,\n"+
    		"8511,ist142020,ist12316,1\n"+
    		"8511,ist24318,ist12316,\n"+
    		"8511,ist24925,ist12316,\n"+
    		"8000,ist12282,,\n"+
    		"8300,ist13267,,\n"+
    		"8010,ist22137,ist12282,\n"+
    		"8010,ist20831,ist12282,\n"+
    		"8010,ist22835,ist12282,\n"+
    		"8010,ist23727,ist12282,\n"+
    		"8100,ist24540,ist12282,\n"+
    		"8100,ist22610,ist24540,\n"+
    		"8100,ist23833,ist24540,\n"+
    		"8020,ist23013,ist12282,\n"+
    		"8020,ist22987,ist23013,\n"+
    		"8020,ist23467,ist23013,\n"+
    		"8020,ist23855,ist23013,\n"+
    		"8020,ist23862,ist23013,\n"+
    		"8020,ist31302,ist23013,\n"+
    		"8020,ist24750,ist23013,\n"+
    		"8021,ist24178,ist23013,\n"+
    		"8021,ist24299,ist24178,\n"+
    		"8021,ist33029,ist24178,1\n"+
    		"8030,ist23827,ist12282,\n"+
    		"8030,ist24068,ist23827,\n"+
    		"8030,ist31597,ist23827,1\n"+
    		"8040,ist23922,ist12919,\n"+
    		"8040,ist20928,ist23922,\n"+
    		"8040,ist21346,ist23922,\n"+
    		"8040,ist22139,ist23922,\n"+
    		"8040,ist22140,ist23922,\n"+
    		"8040,ist22497,ist23922,\n"+
    		"8040,ist22514,ist23922,\n"+
    		"8040,ist22666,ist23922,\n"+
    		"8040,ist22676,ist23922,\n"+
    		"8040,ist22807,ist23922,\n"+
    		"8040,ist23001,ist23922,\n"+
    		"8040,ist23652,ist23922,\n"+
    		"8040,ist23925,ist23922,\n"+
    		"8040,ist24586,ist23922,1\n"+
    		"8040,ist148783,ist23922,1\n"+
    		"8040,ist24098,ist24661,\n"+
    		"8040,ist149873,ist23922,\n"+
    		"8003,ist11051,,\n"+
    		"8003,ist24390,ist11051,\n"+
    		"8003,ist24331,ist11051,1\n"+
    		"8002,ist23541,ist12282,\n"+
    		"8002,ist22697,ist23541,\n"+
    		"8002,ist24620,ist23541,\n"+
    		"8002,ist24803,ist23541,\n"+
    		"8001,ist22353,ist12889,\n"+
    		"8001,ist23025,ist12282,\n"+
    		"8001,ist23044,ist12282,\n"+
    		"8001,ist24196,ist12889,\n"+
    		"0004,ist22237,ist12470,\n"+
    		"0004,ist23053,ist12470,\n"+
    		"0005,ist23311,ist12177,\n"+
    		"0005,ist23886,ist12177,\n"+
    		"2001,ist22996,ist23308,\n"+
    		"2001,ist23056,ist11387,\n"+
    		"2001,ist23308,ist12841,\n"+
    		"2001,ist23651,ist23308,\n"+
    		"2001,ist23654,ist23308,\n"+
    		"2001,ist23655,ist23308,\n"+
    		"2001,ist23685,ist23308,\n"+
    		"2001,ist23745,ist12058,\n"+
    		"2001,ist23879,ist13084,\n"+
    		"2001,ist24076,ist23308,1\n"+
    		"2001,ist24119,ist23308,1\n"+
    		"2001,ist24212,ist12736,\n"+
    		"2001,ist24413,ist12184,\n"+
    		"2001,ist24697,ist12113,1\n"+
    		"2001,ist24702,ist23308,\n"+
    		"2001,ist24739,ist23308,\n"+
    		"2001,ist24757,ist12736,1\n"+
    		"2003,ist23040,ist12191,\n"+
    		"2003,ist23041,ist12191,\n"+
    		"1102,ist24992,ist24577,1\n"+
    		"2013,ist22787,ist12397,\n"+
    		"2013,ist23471,ist12397,\n"+
    		"2013,ist24863,ist12397,\n"+
    		"2014,ist22989,ist11385,\n"+
    		"2014,ist23051,ist11385,\n"+
    		"2009,ist23054,ist11624,\n"+
    		"2009,ist23877,ist11624,\n"+
    		"2009,ist24533,ist11624,\n"+
    		"2009,ist24789,ist11142,\n"+
    		"2010,ist23055,ist24958,\n"+
    		"2011,ist24640,ist12823,\n"+
    		"2012,ist23929,ist13957,\n"+
    		"2020,ist24335,ist12448,\n"+
    		"2020,ist22357,ist12448,\n"+
    		"2020,ist24716,ist12448,\n"+
    		"2040,ist24211,ist12736,\n"+
    		"2040,ist24259,ist12736,\n"+
    		"2040,ist24278,ist12736,\n"+
    		"2040,ist24843,ist12736,\n"+
    		"2040,ist24844,ist12736,\n"+
    		"2013,ist22776,ist12538,\n"+
    		"2013,ist23911,ist12538,\n"+
    		"2003,ist23468,ist14020,\n"+
    		"1608,ist23964,ist12397,\n"+
    		"1608,ist24033,ist12538,\n"+
    		"1608,ist24338,ist12299,\n"+
    		"1608,ist24757,ist12736,1\n"+
    		"1126,ist24058,ist11400,\n"+
    		"1126,ist14344,ist11400,1\n"+
    		"4015,ist23354,ist11781,\n"+
    		"2101,ist21251,ist13132,\n"+
    		"2101,ist22678,ist21561,\n"+
    		"2101,ist22690,ist12503,\n"+
    		"2101,ist23005,ist12503,\n"+
    		"2101,ist23022,ist21561,\n"+
    		"2101,ist23037,ist21561,\n"+
    		"2101,ist23154,ist21561,\n"+
    		"2101,ist23695,ist12503,\n"+
    		"2101,ist23696,ist21561,\n"+
    		"2101,ist23800,ist21561,\n"+
    		"2101,ist23808,ist21561,\n"+
    		"2101,ist23822,ist21561,\n"+
    		"2101,ist24168,ist12503,\n"+
    		"2101,ist24273,ist11049,1\n"+
    		"2101,ist24886,ist11049,\n"+
    		"2112,ist22512,ist11049,\n"+
    		"2112,ist22899,ist11049,\n"+
    		"2112,ist22992,ist11049,\n"+
    		"2112,ist23033,ist11049,\n"+
    		"2112,ist23815,ist11049,\n"+
    		"2113,ist21515,ist11393,\n"+
    		"2113,ist22780,ist11393,\n"+
    		"2113,ist22852,ist11393,\n"+
    		"2113,ist24310,ist11393,\n"+
    		"2115,ist21561,ist12403,\n"+
    		"2115,ist23038,ist12403,\n"+
    		"2115,ist23464,ist12403,\n"+
    		"2115,ist23881,ist12403,\n"+
    		"2115,ist136657,ist12403,1\n"+
    		"2115,ist33043,ist12403,\n"+
    		"2115,ist164287,ist12403,\n"+
    		"2115,ist33045,ist12403,\n"+
    		"2111,ist22148,ist12270,\n"+
    		"2111,ist22504,ist12270,\n"+
    		"2114,ist22475,ist11994,\n"+
    		"2114,ist22660,ist11994,\n"+
    		"2120,ist22513,ist13771,\n"+
    		"2120,ist22657,ist13771,\n"+
    		"2120,ist23717,ist13771,\n"+
    		"1601,ist23031,ist11412,\n"+
    		"1601,ist23850,ist11814,\n"+
    		"1601,ist24542,ist11412,\n"+
    		"1601,ist24543,ist11412,\n"+
    		"1601,ist24626,ist12760,1\n"+
    		"1601,ist146492,ist11814,\n"+
    		"1601,ist90241,ist30621,\n"+
    		"2201,ist20933,ist11440,\n"+
    		"2201,ist22684,ist12272,\n"+
    		"2201,ist23015,ist12272,\n"+
    		"2201,ist23219,ist12272,\n"+
    		"2201,ist23552,ist12272,\n"+
    		"2201,ist23656,ist12272,\n"+
    		"2201,ist23705,ist11440,\n"+
    		"2201,ist23746,ist12272,\n"+
    		"2201,ist23813,ist12855,\n"+
    		"2201,ist24260,ist12272,1\n"+
    		"2214,ist20943,ist11440,\n"+
    		"2214,ist24249,ist11440,\n"+
    		"2211,ist22242,ist11388,\n"+
    		"2211,ist22491,ist11388,\n"+
    		"2211,ist22683,ist11388,\n"+
    		"2211,ist22739,ist11388,\n"+
    		"2215,ist22966,ist13722,\n"+
    		"2215,ist23066,ist13722,\n"+
    		"2215,ist23669,ist13722,\n"+
    		"2215,ist24502,ist13722,\n"+
    		"2210,ist21919,ist11668,\n"+
    		"2210,ist22331,ist11668,\n"+
    		"2210,ist22344,ist11668,\n"+
    		"2210,ist23726,ist11668,\n"+
    		"2216,ist23030,ist11668,\n"+
    		"2216,ist23065,ist11668,\n"+
    		"2216,ist23473,ist11668,\n"+
    		"2216,ist23660,ist11668,\n"+
    		"2213,ist23631,ist12694,\n"+
    		"2213,ist23757,ist12694,\n"+
    		"2212,ist22991,ist13986,\n"+
    		"2212,ist23551,ist13986,\n"+
    		"2212,ist24794,ist13986,\n"+
    		"2220,ist21460,ist12093,\n"+
    		"2220,ist23071,ist12093,\n"+
    		"1602,ist22524,ist11716,\n"+
    		"1602,ist23940,ist11716,\n"+
    		"1602,ist24186,ist11716,\n"+
    		"1602,ist24246,ist11716,\n"+
    		"1602,ist24633,ist11388,\n"+
    		"1602,ist24951,ist11716,\n"+
    		"1137,ist32802,ist12361,\n"+
    		"1137,ist32386,ist12361,\n"+
    		"1137,ist145517,ist12361,1\n"+
    		"2217,ist23034,ist11869,\n"+
    		"2217,ist23706,ist11869,\n"+
    		"2217,ist23846,ist11869,\n"+
    		"2217,ist23858,ist11869,\n"+
    		"1133,ist23825,ist11869,\n"+
    		"2301,ist20927,ist12081,\n"+
    		"2301,ist20979,ist12081,\n"+
    		"2301,ist21489,ist11648,\n"+
    		"2301,ist21526,ist12451,\n"+
    		"2301,ist22235,ist12081,\n"+
    		"2301,ist22653,ist12662,\n"+
    		"2301,ist22769,ist11177,\n"+
    		"2301,ist22777,ist11177,\n"+
    		"2301,ist22836,ist12081,\n"+
    		"2301,ist22982,ist20979,\n"+
    		"2301,ist23016,ist12081,\n"+
    		"2301,ist129875,ist12442,\n"+
    		"2301,ist23627,ist11988,\n"+
    		"2301,ist23849,ist11648,\n"+
    		"2301,ist23884,ist12081,\n"+
    		"2301,ist24300,ist12268,1\n"+
    		"2301,ist31441,ist12051,\n"+
    		"2301,ist32955,ist12081,\n"+
    		"2301,ist24967,ist12081,1\n"+
    		"2320,ist23344,ist12919,\n"+
    		"2320,ist23920,ist12919,\n"+
    		"2320,ist24079,ist12919,1\n"+
    		"2320,ist24127,ist12919,1\n"+
    		"1144,ist46848,ist10480,1\n"+
    		"1144,ist25056,ist10480,1\n"+
    		"2401,ist22648,ist11579,\n"+
    		"2401,ist22675,ist11579,\n"+
    		"2401,ist22974,ist12617,\n"+
    		"2401,ist22983,ist12173,\n"+
    		"2401,ist23010,ist11579,\n"+
    		"2401,ist23017,ist12173,\n"+
    		"2401,ist23024,ist11579,\n"+
    		"2401,ist23206,ist12173,\n"+
    		"2401,ist23309,ist12173,\n"+
    		"2401,ist23797,ist12617,\n"+
    		"2401,ist23836,ist12173,\n"+
    		"2420,ist22511,ist12173,\n"+
    		"2420,ist22977,ist12173,\n"+
    		"2420,ist23668,ist12173,\n"+
    		"2420,ist23797,ist12173,\n"+
    		"2501,ist21510,ist10876,\n"+
    		"2501,ist21530,ist10876,\n"+
    		"2501,ist22474,ist10876,\n"+
    		"2501,ist22689,ist10876,\n"+
    		"2501,ist23046,ist10876,\n"+
    		"2501,ist23625,ist10876,\n"+
    		"2501,ist23809,ist10876,\n"+
    		"2501,ist23926,ist10876,\n"+
    		"2501,ist24271,ist10876,\n"+
    		"2501,ist24398,ist10876,\n"+
    		"2501,ist24644,ist10876,\n"+
    		"2501,ist24898,ist10876,\n"+
    		"1143,ist90144,ist12180,\n"+
    		"2602,ist20939,ist13730,\n"+
    		"2602,ist21295,ist13730,\n"+
    		"2602,ist23061,ist13730,\n"+
    		"2603,ist23062,ist12726,\n"+
    		"2604,ist20941,ist12175,\n"+
    		"2604,ist22645,ist12175,\n"+
    		"1143,ist25020,ist12726,1\n"+
    		"1143,ist164270,ist12726,1\n"+
    		"1143,ist153127,ist12726,1\n"+
    		"2701,ist22246,ist12729,\n"+
    		"2701,ist22343,ist12729,\n"+
    		"2701,ist22651,ist12729,\n"+
    		"2701,ist22968,ist12729,\n"+
    		"2701,ist24254,ist12729,\n"+
    		"1604,ist23049,ist11440,\n"+
    		"1604,ist24238,ist11440,\n"+
    		"1604,ist24971,ist11440,1\n"+
    		"2801,ist21708,ist13898,\n"+
    		"2801,ist22342,ist13898,\n"+
    		"2801,ist22652,ist13898,\n"+
    		"2801,ist22984,ist13898,\n"+
    		"2801,ist23023,ist13898,\n"+
    		"2801,ist23565,ist13898,\n"+
    		"2801,ist23831,ist13898,\n"+
    		"2801,ist23837,ist13898,\n"+
    		"2801,ist24302,ist13898,\n"+
    		"2801,ist24303,ist13898,\n"+
    		"2801,ist24322,ist13898,1\n"+
    		"2801,ist24330,ist13898,1\n"+
    		"2801,ist24386,ist13898,\n"+
    		"2801,ist24537,ist13898,1\n"+
    		"2801,ist141687,ist13898,1\n"+
    		"2901,ist23074,ist12631,\n"+
    		"2901,ist24422,ist12631,\n"+
    		"2901,ist24993,ist12631,1\n"+
    		"2901,ist25104,ist12631,1\n"+
    		"7610,ist12922,,\n"+
    		"7610,ist25115,ist12922,1\n"+
    		"7621,ist23536,ist12922,\n"+
    		"7621,ist22670,ist23536,\n"+
    		"7621,ist23619,ist12922,\n"+
    		"7621,ist24555,ist23536,\n"+
    		"7621,ist24793,ist23536,\n"+
    		"7640,ist23821,ist12922,\n"+
    		"7640,ist22644,ist23821,\n"+
    		"7640,ist23303,ist23821,\n"+
    		"7640,ist23896,ist23821,\n"+
    		"7640,ist23921,ist23821,\n"+
    		"7640,ist23928,ist23821,\n"+
    		"7640,ist24546,ist23821,\n"+
    		"7640,ist24714,ist23821,\n"+
    		"7630,ist11086,,\n"+
    		"7630,ist22980,ist11086,\n"+
    		"7630,ist24266,ist11086,\n"+
    		"7630,ist24332,ist11086,\n"+
    		"7630,ist24384,ist11086,\n"+
    		"7630,ist24547,ist11086,\n"+
    		"7630,ist24549,ist11086,\n"+
    		"7630,ist24606,ist11086,1\n"+
    		"7630,ist24660,ist11086,1\n"+
    		"7630,ist24670,ist11086,1\n"+
    		"7630,ist24713,ist11086,\n"+
    		"7630,ist25015,ist11086,1\n"+
    		"7630,ist127877,ist11086,1\n"+
    		"7650,ist14497,,\n"+
    		"7650,ist22753,ist14497,\n"+
    		"7650,ist22756,ist14497,\n"+
    		"7650,ist24403,ist14497,\n"+
    		"7650,ist148793,ist14497,1\n"+
    		"7660,ist24630,,\n"+
    		"7660,ist22979,ist24630,\n"+
    		"7660,ist23901,ist24630,\n"+
    		"1145,ist23007,ist11391,\n"+
    		"1145,ist23043,ist11391,\n"+
    		"1145,ist23059,ist11011,\n"+
    		"1145,ist23067,ist11391,\n"+
    		"1145,ist23070,ist11391,\n"+
    		"1145,ist23295,ist11391,\n"+
    		"1145,ist23380,ist11391,\n"+
    		"1145,ist23480,ist11391,\n"+
    		"1145,ist23482,ist11391,\n"+
    		"1145,ist23562,ist13387,\n"+
    		"1145,ist23563,ist13387,\n"+
    		"1145,ist23636,ist11391,\n"+
    		"1145,ist23691,ist13387,\n"+
    		"1145,ist23817,ist23844,\n"+
    		"1145,ist23823,ist11391,\n"+
    		"1145,ist23838,ist11391,\n"+
    		"1145,ist23842,ist11011,\n"+
    		"1145,ist23853,ist11011,\n"+
    		"1145,ist23871,ist23844,\n"+
    		"1145,ist23878,ist11391,\n"+
    		"1145,ist23885,ist11391,\n"+
    		"1145,ist23899,ist11011,\n"+
    		"1145,ist24206,ist11391,\n"+
    		"1145,ist24365,ist13387,\n"+
    		"1145,ist24637,ist11391,\n"+
    		"1145,ist148177,ist12916,\n"+
    		"1145,ist24658,ist11391,\n"+
    		"1145,ist24668,ist11391,\n"+
    		"1145,ist130867,ist24515,\n"+
    		"1145,ist24918,ist13387,\n"+
    		"1145,ist25005,ist13387,1\n"+
    		"1145,ist149536,ist11391,1\n"+
    		"8050,ist23868,ist11648,\n"+
    		"8050,ist22778,ist11648,\n"+
    		"8050,ist24426,ist22778,\n"+
    		"8050,ist24430,ist22778,\n"+
    		"8050,ist24435,ist22778,\n"+
    		"8050,ist24438,ist22778,\n"+
    		"8050,ist24447,ist22778,\n"+
    		"8050,ist24448,ist22778,\n"+
    		"8050,ist24453,ist22778,\n"+
    		"8050,ist24553,ist22778,1\n"+
    		"8050,ist24733,ist22778,1\n"+
    		"8050,ist24734,ist22778,1\n"+
    		"8050,ist24743,ist22778,1\n"+
    		"8050,ist24783,ist22778,1\n"+
    		"8050,ist24784,ist22778,1\n"+
    		"8050,ist24787,ist22778,1\n"+
    		"8050,ist23835,ist11648,\n"+
    		"8050,ist24428,ist23835,\n"+
    		"8050,ist24431,ist23835,\n"+
    		"8050,ist24445,ist23835,\n"+
    		"8050,ist24499,ist23835,\n"+
    		"8050,ist163826,ist23835,1\n"+
    		"8050,ist23872,ist11648,\n"+
    		"8050,ist24427,ist23872,\n"+
    		"8050,ist24429,ist23872,\n"+
    		"8050,ist24456,ist23872,\n"+
    		"8050,ist24785,ist23872,1\n"+
    		"8050,ist21168,ist11648,\n"+
    		"8050,ist23820,ist21168,\n"+
    		"8050,ist23900,ist21168,\n"+
    		"8050,ist24147,ist21168,\n"+
    		"8050,ist24148,ist21168,\n"+
    		"8050,ist24432,ist21168,\n"+
    		"8050,ist24433,ist21168,\n"+
    		"8050,ist24444,ist21168,\n"+
    		"8050,ist24446,ist21168,\n"+
    		"8050,ist24451,ist21168,\n"+
    		"8050,ist24452,ist21168,\n"+
    		"8050,ist24455,ist21168,\n"+
    		"8050,ist24458,ist21168,\n"+
    		"8050,ist24507,ist21168,1\n"+
    		"8050,ist24732,ist21168,1\n"+
    		"8050,ist151317,ist21168,1\n"+
    		"8050,ist24848,ist21168,1\n"+
    		"8050,ist24881,ist21168,1\n"+
    		"8050,ist24956,ist21168,1\n"+
    		"8050,ist25025,ist21168,1\n"+
    		"8050,ist25026,ist21168,1\n"+
    		"8050,ist25106,ist21168,1\n"+
    		"8050,ist25111,ist21168,1\n"+
    		"8050,ist25118,ist21168,1\n"+
    		"8050,ist23870,ist11648,\n"+
    		"6202,ist23594,ist10984,\n"+
    		"6202,ist23599,ist10984,\n"+
    		"6202,ist23600,ist23594,\n"+
    		"6202,ist23606,ist23594,\n"+
    		"6202,ist23607,ist23594,\n"+
    		"6202,ist23608,ist23594,\n"+
    		"6202,ist23609,ist10984,\n"+
    		"6202,ist23616,ist12219,\n"+
    		"6202,ist23617,ist23594,\n"+
    		"6202,ist23723,ist23594,\n"+
    		"6202,ist23724,ist23594,\n"+
    		"6202,ist24116,ist12219,\n"+
    		"6202,ist24522,ist23594,\n"+
    		"6202,ist90358,ist23594,\n"+
    		"1116,ist23576,ist10800,\n"+
    		"1113,ist23571,ist11805,\n"+
    		"1113,ist24268,ist11938,\n"+
    		"1113,ist24724,ist11938,1\n"+
    		"1115,ist23741,ist11165,\n"+
    		"1115,ist23743,ist11165,\n"+
    		"1115,ist23566,ist11344,\n");


    @Override
    public void doIt() {
	try {
//	    FileInputStream fstream = new FileInputStream("/home/joantune/CIIST-Wspace/siadap-import-with-adist.csv");
		StringReader csvContentReader = new StringReader(csvContent);
	    // Get the object of DataInputStream
//	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(csvContentReader);
	    String strLine;
	    // Read File Line By Line
	    while ((strLine = br.readLine()) != null) {
		processLine(strLine);
	    }
	    // Close the input stream
//	    in.close();
	} catch (IOException e) {// Catch exception if any
	    out.println("Error: " + e.getMessage());
	}

	out.println(avaliators.size());

	for (Integer costCenter : avaliators.keySet()) {
	    List<Evaluator> evaluators = avaliators.get(costCenter);
	    String centerString = costCenter.toString();
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    Person responsible = null;
	    int max = 0;
	    for (Evaluator evaluator : evaluators) {
		User user = User.findByUsername(evaluator.istId);
		if (evaluator.size() > max) {
		    responsible = user.getPerson();
		    max = evaluator.size();
		}
	    }
	    responsibles.add(new Responsible(responsible.getUser(), c));
	}

	ServiceManager.execute(new ServicePredicate() {

	    @Override
	    public void execute() {
		migrateStuff(avaliators, responsibles);
	    }

	});

    }

    private void migrateStuff(Map<Integer, List<Evaluator>> avaliators2, List<Responsible> responsibles2) {
	out.println("STarting");
	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	AccountabilityType workingRelation = configuration.getWorkingRelation();
	AccountabilityType workingRelationWithNoQuota = configuration.getWorkingRelationWithNoQuota();

	for (Responsible responsible : responsibles) {
	    Unit unit = responsible.getCenter().getUnit();
	    Person person = responsible.getUser().getPerson();
	    if (!unit.getChildPersons(evaluationRelation).contains(person)) {
		person.addParent(unit, evaluationRelation, today, null);
	    }
	}

	for (Integer costCenter : avaliators.keySet()) {
	    List<Evaluator> evaluators = avaliators.get(costCenter);
	    String centerString = costCenter.toString();
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    Unit unit = c.getUnit();

	    for (Evaluator evaluator : evaluators) {
		Person evaluatorPerson = evaluator.getUser().getPerson();
		if (evaluatorPerson == null) {
		    out.println("WTF: " + evaluator.getUser().getUsername());
		}
		if (evaluatorPerson.getParentUnits(evaluationRelation) == null) {
		    out.println("WTF-relation for: " + evaluator.getUser().getUsername());
		}
		boolean isResponsible = evaluatorPerson.getParentUnits(evaluationRelation).contains(unit);
		for (Evaluated evaluated : evaluator.evaluated) {
		    User user = evaluated.getUser();
		    boolean adist = evaluated.getAdist();
		    if (user == null) {
			out.println(evaluated.istId + " NO USER");
			user = new User(evaluated.istId);
		    }
		    Person evaluatedPerson = user.getPerson();
		    if (evaluatedPerson == null) {
			out.println(evaluated.istId + " NO PERSON");
			String personName = ImportSiadapStructure.unknownUsersMap.get(evaluated.istId);
			if (personName == null) {
			    out.println("Found no user in table");
			}
			evaluatedPerson = Person.create(MultiLanguageString.i18n().add("pt", personName).finish(), Person
				.getPartyTypeInstance());
			evaluatedPerson.setUser(user);
		    }

		    if (!adist && !unit.getChildPersons(workingRelation).contains(evaluatedPerson)) {
			evaluatedPerson.addParent(unit, workingRelation, today, null);
		    }
		    if (adist && !unit.getChildPersons(workingRelationWithNoQuota).contains(evaluatedPerson)) {
			evaluatedPerson.addParent(unit, workingRelationWithNoQuota, today, null);
		    }
		    if (!isResponsible) {
			if (!evaluatorPerson.getChildPersons(evaluationRelation).contains(evaluatedPerson)) {
			    evaluatedPerson.addParent(evaluatorPerson, evaluationRelation, today, null);
			}
		    }
		}
	    }
	}
	out.println("DONE!");
    }

    private void processLine(String strLine) {
	String[] values = strLine.split(",");

	if (values.length < 3 || values.length > 5) {
	    out.println("skipped: " + strLine);
	    return;
	}
	String cc = values[0].trim();
	Integer ccNumber = Integer.valueOf(cc);
	String evaluatorId = values[2].trim();
	String evaluatedId = values[1].trim();
	Boolean adist = values.length == 4 ? values[3].trim().equals("1") : false;

	if (StringUtils.isEmpty(evaluatorId)) {
	    return;
	}

	List<Evaluator> evaluators = avaliators.get(ccNumber);
	if (evaluators == null) {
	    out.println("Starting evalutors for " + cc);
	    evaluators = new ArrayList<Evaluator>();
	    avaliators.put(ccNumber, evaluators);
	}
	boolean added = false;
	for (Evaluator evaluator : evaluators) {
	    if (evaluator.match(evaluatorId)) {
		added = true;
		out.println("Adding existing evaluator " + evaluatorId + " for " + cc + "adist: " + adist);
		evaluator.addEvaluated(evaluatedId, adist);
	    }
	}
	if (!added) {
	    out.println("New evaluator " + evaluatorId + " for " + cc + " adist: " + adist);
	    Evaluator evaluator = new Evaluator(evaluatorId);
	    evaluator.addEvaluated(evaluatedId, adist);
	    evaluators.add(evaluator);
	}
    }

    public static class Evaluator {
	String istId;
	List<Evaluated> evaluated;

	public Evaluator(String istId) {
	    this.istId = istId.trim();
	    evaluated = new ArrayList<Evaluated>();
	}

	public void addEvaluated(String istId, Boolean adist) {
	    evaluated.add(new Evaluated(istId, adist));
	}

	public boolean match(String istId) {
	    return this.istId.equals(istId);
	}

	public int size() {
	    return evaluated.size();
	}

	public User getUser() {
	    return User.findByUsername(istId);
	}
    }

    public static class Evaluated {
	String istId;
	Boolean adist;

	public Evaluated(String istId, Boolean adist) {
	    this.istId = istId.trim();
	    this.adist = adist;
	}

	public boolean getAdist() {
	    return adist;
	}

	public User getUser() {
	    return User.findByUsername(istId);
	}
    }

    public static class Responsible {
	User user;
	CostCenter center;

	public Responsible(User user, CostCenter center) {
	    this.user = user;
	    this.center = center;
	}

	public User getUser() {
	    return user;
	}

	public CostCenter getCenter() {
	    return center;
	}

    }
}
