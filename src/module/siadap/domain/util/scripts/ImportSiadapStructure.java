package module.siadap.domain.util.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.predicates.PartyPredicate;
import module.organizationIst.domain.listner.LoginListner;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import myorg.domain.User;
import myorg.domain.scheduler.ReadCustomTask;
import myorg.domain.scheduler.TransactionalThread;

import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class ImportSiadapStructure extends ReadCustomTask {

    Map<Integer, List<Evaluator>> avaliators = new HashMap<Integer, List<Evaluator>>();
    List<Responsible> responsibles = new ArrayList<Responsible>();
    //    public static Map<String, String> unknownUsersMap = new HashMap<String, String>();

    public static final boolean debugModeOn = true;

    /**
     * Contains all of the User objects that have been mapped by this script
     */
    //    public static Set<User> mappedUsers = new HashSet<User>();

    /**
     * Assert if one should go over the Evaluation relations and end them if the
     * users aren't on this script. NOTE: Basicly this removes the previous
     * users that weren't on the list and should only be done if the evaluation
     * relation is exclusive to SIADAP under the penalty of messing up another
     * relation having an unwanted impact on other Workflows/applications
     */
    public static final boolean removeEvaluationRelationsForUnmappedUsers = false;
    private static boolean inDevelopmentSystem = false;

    //    static {
    //	unknownUsersMap.put("ist25068", "Pedro Miguel Simões Coito");
    //	unknownUsersMap.put("ist25072", "Alvarinho Carvalho do Espirito Santo");
    //	unknownUsersMap.put("ist25079", "Adolfo Pereira Moura");
    //	unknownUsersMap.put("ist25095", "Marcelo Gurgel Figueiredo Moleiro");
    //	unknownUsersMap.put("ist25096", "Rute Catarina Panaças Guerreiro");
    //	unknownUsersMap.put("ist25104", "Teresa Jacinto de Oliveira Marques");
    //	unknownUsersMap.put("ist25106", "Miriam Mano Ferreira");
    //	unknownUsersMap.put("ist25109", "Elisabete Moreira de Oliveira Pino");
    //	unknownUsersMap.put("ist25110", "Dino Rodrigues Pereira das Neves");
    //	unknownUsersMap.put("ist25111", "Fábio André Duarte Morgado");
    //	unknownUsersMap.put("ist25112", "Hugo Alexandre Gonçalves Furtado");
    //	unknownUsersMap.put("ist25115", "Sandra Nazaré Gomes da Fonseca");
    //	unknownUsersMap.put("ist25118", "Joana Alves Lindinho Nunes de Castro");
    //	unknownUsersMap.put("ist25121", "Aurora Bonfim de Carvalho Oliveira");
    //	unknownUsersMap.put("ist25125", "Tiago Luís Ramos Silva Machado");
    //    }

    ArrayList<String> costCentersMartelados = new ArrayList<String>();

    private static ArrayList<String> originalRelationsList = new ArrayList<String>();
    private static ArrayList<String> systemsRelationsList = new ArrayList<String>();

    private final static String csvContent = new String("8001,ist22353,ist12889,\n" + "8001,ist23025,ist22353,\n"
	    + "8001,ist23044,ist22353,\n" + "0004,ist22237,ist12470,\n" + "0004,ist23053,ist12470,\n"
	    + "0005,ist23886,ist12177,\n" + "6100,ist24235,ist12282,\n" + "6100,ist24728,ist24235,\n"
	    + "8002,ist23541,ist12282,\n" + "8002,ist22697,ist23541,\n" + "8002,ist24620,ist23541,\n"
	    + "8002,ist24803,ist23541,\n" + "8003,ist24331,ist11051,\n" + "8003,ist24390,ist11051,\n"
	    + "8010,ist20831,ist22137,\n" + "8010,ist22137,ist12282,\n" + "8010,ist22835,ist12282,\n"
	    + "8020,ist23013,ist12282,\n" + "8020,ist22987,ist23013,\n" + "8020,ist23467,ist23013,\n"
	    + "8020,ist23855,ist23013,\n" + "8020,ist23862,ist23013,\n" + "8020,ist23930,ist23013,\n"
	    + "8020,ist90120,ist23013,\n" + "8020,ist31302,ist23013,\n" + "8020,ist24750,ist23013,\n"
	    + "8021,ist24178,ist23013,\n" + "8021,ist24299,ist24178,\n" + "8021,ist33029,ist24178,\n"
	    + "8030,ist23827,ist12282,\n" + "8030,ist24068,ist23827,\n" + "8040,ist25186,ist12919,\n"
	    + "8040,ist23652,ist25186,\n" + "8040,ist20928,ist25186,\n" + "8040,ist22139,ist25186,\n"
	    + "8040,ist22140,ist25186,\n" + "8040,ist22514,ist25186,\n" + "8040,ist22666,ist25186,\n"
	    + "8040,ist22676,ist25186,\n" + "8040,ist22807,ist25186,\n" + "8040,ist23001,ist25186,\n"
	    + "8040,ist23925,ist25186,\n" + "8040,ist22497,ist25186,\n" + "8040,ist149873,ist25186,\n"
	    + "8100,ist24540,ist12282,\n" + "8100,ist23833,ist24540,\n" + "8100,ist22610,ist24540,\n"
	    + "7610,ist23004,ist12922,\n" + "7610,ist23619,ist12922,\n" + "7610,ist23928,ist12922,\n"
	    + "7621,ist23536,ist12922,\n" + "7621,ist22670,ist23536,\n" + "7621,ist24266,ist23536,\n"
	    + "7621,ist24555,ist23536,\n" + "7630,ist24384,ist11086,\n" + "7630,ist24547,ist11086,\n"
	    + "7630,ist24549,ist11086,\n" + "7630,ist24713,ist11086,\n" + "7630,ist22980,ist11086,\n"
	    + "7640,ist23821,ist12922,\n" + "7640,ist22644,ist23821,\n" + "7640,ist23303,ist23821,\n"
	    + "7640,ist23896,ist23821,\n" + "7640,ist23921,ist23821,\n" + "7640,ist24714,ist23821,\n"
	    + "7650,ist24403,ist14497,\n" + "7650,ist22756,ist14497,\n" + "7660,ist22979,ist24630,\n"
	    + "7660,ist23901,ist24630,\n" + "6211,ist23674,ist24235,\n" + "6211,ist23020,ist23674,\n"
	    + "6211,ist10482794,ist23674,\n" + "6212,ist22337,ist23674,\n" + "6212,ist21902,ist22337,\n"
	    + "6212,ist22643,ist22337,\n" + "6212,ist22997,ist22337,\n" + "6212,ist22998,ist22337,\n"
	    + "6212,ist23474,ist22337,\n" + "6212,ist23723,ist22337,\n" + "6212,ist24753,ist22337,\n"
	    + "6212,ist24843,ist22337,\n" + "6212,ist24168,ist22337,\n" + "6213,ist23223,ist23674,\n"
	    + "6213,ist24292,ist23223,\n" + "6213,ist22669,ist23223,\n" + "6213,ist24454,ist23223,\n"
	    + "6215,ist24661,ist23674,\n" + "6215,ist24129,ist24661,\n" + "6215,ist23558,ist24661,\n"
	    + "6215,ist22352,ist24661,\n" + "6215,ist23343,ist24661,\n" + "6215,ist24349,ist24661,\n"
	    + "6215,ist24641,ist24661,\n" + "6215,ist24676,ist24661,\n" + "6221,ist23461,ist24235,\n"
	    + "6221,ist22328,ist23461,\n" + "6221,ist23063,ist23461,\n" + "6223,ist23570,ist23461,\n"
	    + "6223,ist22969,ist23570,\n" + "6223,ist22233,ist23570,\n" + "6223,ist23475,ist23570,\n"
	    + "6223,ist23916,ist23570,\n" + "6223,ist22510,ist23570,\n" + "6224,ist21903,ist23461,\n"
	    + "6224,ist23620,ist21903,\n" + "6224,ist22520,ist21903,\n" + "6224,ist22717,ist21903,\n"
	    + "6224,ist23623,ist21903,\n" + "6224,ist23667,ist21903,\n" + "6224,ist23752,ist21903,\n"
	    + "6224,ist23756,ist21903,\n" + "6224,ist22755,ist21903,\n" + "6224,ist22798,ist21903,\n"
	    + "6224,ist22349,ist21903,\n" + "6224,ist23830,ist21903,\n" + "6224,ist24305,ist21903,\n"
	    + "6224,ist24663,ist21903,\n" + "6224,ist24884,ist21903,\n" + "6224,ist24965,ist21903,\n"
	    + "6224,ist24985,ist21903,\n" + "6241,ist24588,ist24235,\n" + "6241,ist24522,ist24588,\n"
	    + "6241,ist24700,ist24588,\n" + "6242,ist25185,ist24588,\n" + "6242,ist24053,ist25185,\n"
	    + "6242,ist23052,ist25185,\n" + "6242,ist23465,ist25185,\n" + "6242,ist24055,ist25185,\n"
	    + "6242,ist24512,ist25185,\n" + "6242,ist24961,ist25185,\n" + "6242,ist24962,ist25185,\n"
	    + "6242,ist24056,ist25185,\n" + "6243,ist23248,ist24588,\n" + "6244,ist24059,ist24588,\n"
	    + "6244,ist23484,ist24059,\n" + "6244,ist24532,ist24059,\n" + "6244,ist24541,ist24059,\n"
	    + "6244,ist24741,ist24059,\n" + "6244,ist32393,ist24059,\n" + "8211,ist22592,ist12760,\n"
	    + "8211,ist23832,ist22592,\n" + "8211,ist23472,ist22592,\n" + "8211,ist23811,ist22592,\n"
	    + "8211,ist23857,ist22592,\n" + "8211,ist32181,ist22592,\n" + "8212,ist23880,ist12760,\n"
	    + "8212,ist30615,ist12760,\n" + "6401,ist24877,ist24235,\n" + "6401,ist22674,ist24877,\n"
	    + "6410,ist23932,ist24877,\n" + "6411,ist23478,ist23932,\n" + "6411,ist23409,ist23478,\n"
	    + "6412,ist22752,ist23932,\n" + "6412,ist23202,ist22752,\n" + "6412,ist22329,ist22752,\n"
	    + "6412,ist24252,ist22752,\n" + "6413,ist22751,ist23932,\n" + "6413,ist24954,ist22751,\n"
	    + "6421,ist21303,ist23932,\n" + "6421,ist20940,ist21303,\n" + "6421,ist22758,ist21303,\n"
	    + "6421,ist23889,ist21303,\n" + "6421,ist24888,ist21303,\n" + "6422,ist23647,ist23932,\n"
	    + "6422,ist21768,ist23647,\n" + "6422,ist22686,ist23647,\n" + "6422,ist24385,ist23647,\n"
	    + "6422,ist24769,ist23647,\n" + "6301,ist23470,ist24235,\n" + "6301,ist22990,ist23470,\n"
	    + "6301,ist21493,ist23470,\n" + "6301,ist22680,ist23470,\n" + "6301,ist22800,ist23470,\n"
	    + "6301,ist23638,ist23470,\n" + "6301,ist23560,ist23470,\n" + "6301,ist22492,ist23470,\n"
	    + "6306,ist23022,ist23470,\n" + "6306,ist22500,ist23022,\n" + "6306,ist23599,ist23022,\n"
	    + "6311,ist22757,ist23470,\n" + "6312,ist22234,ist22757,\n" + "6312,ist24225,ist22234,\n"
	    + "6312,ist24229,ist22234,\n" + "6313,ist23555,ist22757,\n" + "6313,ist23073,ist23555,\n"
	    + "6313,ist23804,ist23555,\n" + "6313,ist24935,ist23555,\n" + "6314,ist23306,ist21692,\n"
	    + "6314,ist22685,ist22757,\n" + "6314,ist21692,ist22757,\n" + "6315,ist10482756,ist22757,\n"
	    + "6315,ist22483,ist10482756,\n" + "6315,ist22672,ist10482756,\n" + "6315,ist23011,ist10482756,\n"
	    + "6315,ist23048,ist10482756,\n" + "6315,ist25021,ist10482756,\n" + "6315,ist21488,ist10482756,\n"
	    + "6315,ist24394,ist10482756,\n" + "6316,ist23711,ist22757,\n" + "6316,ist21496,ist23711,\n"
	    + "6331,ist23727,ist23470,\n" + "6332,ist10482686,ist23727,\n" + "6332,ist22519,ist10482686,\n"
	    + "6332,ist22773,ist10482686,\n" + "6332,ist23045,ist10482686,\n" + "6332,ist23559,ist10482686,\n"
	    + "6332,ist23735,ist10482686,\n" + "6332,ist22824,ist10482686,\n" + "6332,ist25109,ist10482686,\n"
	    + "6333,ist23057,ist23727,\n" + "6333,ist20828,ist23057,\n" + "6333,ist22521,ist23057,\n"
	    + "6333,ist22836,ist23057,\n" + "6333,ist23466,ist23057,\n" + "6333,ist22967,ist23057,\n"
	    + "6333,ist137325,ist23057,\n" + "6333,ist144371,ist23057,\n" + "6334,ist45951,ist23727,\n"
	    + "6334,ist23828,ist45951,\n" + "6334,ist24999,ist45951,\n" + "8310,ist23703,ist13267,\n"
	    + "8311,ist23978,ist23703,\n" + "8311,ist22978,ist23978,\n" + "8311,ist21268,ist23978,\n"
	    + "8311,ist22973,ist23978,\n" + "8311,ist23919,ist23978,\n" + "8311,ist21272,ist23978,\n"
	    + "8311,ist24065,ist23978,\n" + "8311,ist24329,ist23978,\n" + "8311,ist24546,ist23978,\n"
	    + "8311,ist24896,ist23978,\n" + "8312,ist23003,ist23703,\n" + "8312,ist23861,ist23703,\n"
	    + "8312,ist23068,ist23703,\n" + "8312,ist23720,ist23703,\n" + "8312,ist22142,ist23703,\n"
	    + "8312,ist23028,ist23703,\n" + "8312,ist23700,ist23703,\n" + "8312,ist23860,ist23703,\n"
	    + "8312,ist23926,ist23703,\n" + "8312,ist24545,ist23703,\n" + "8312,ist24840,ist23703,\n"
	    + "8312,ist24230,ist23703,\n" + "8313,ist24412,ist23703,\n" + "8313,ist24414,ist24412,\n"
	    + "8313,ist22330,ist24412,\n" + "8313,ist22498,ist24412,\n" + "8313,ist22659,ist24412,\n"
	    + "8313,ist22975,ist24412,\n" + "8313,ist22986,ist24412,\n" + "8313,ist23890,ist24412,\n"
	    + "8314,ist23718,ist23703,\n" + "8314,ist23038,ist23718,\n" + "8314,ist24195,ist23718,\n"
	    + "8314,ist90146,ist23718,\n" + "8401,ist21846,ist12048,\n" + "8401,ist138407,ist12048,\n"
	    + "8401,ist23021,ist21846,\n" + "8401,ist23487,ist21846,\n" + "8401,ist24698,ist13499,\n"
	    + "8401,ist24123,ist13499,\n" + "8410,ist24459,ist12048,\n" + "8411,ist24462,ist24459,\n"
	    + "8411,ist22207,ist24459,\n" + "8411,ist24717,ist24459,\n" + "8412,ist23000,ist24459,\n"
	    + "8412,ist23883,ist23000,\n" + "8412,ist22240,ist23000,\n" + "8412,ist21901,ist23000,\n"
	    + "8412,ist21505,ist148793,\n" + "8413,ist148793,ist24459,\n" + "8413,ist22862,ist148793,\n"
	    + "8413,ist22154,ist148793,\n" + "8413,ist21774,ist148793,\n" + "8413,ist22245,ist148793,\n"
	    + "8413,ist22619,ist148793,\n" + "8413,ist22665,ist148793,\n" + "8413,ist22753,ist148793,\n"
	    + "8420,ist24518,ist13499,\n" + "8421,ist24421,ist24518,\n" + "8421,ist153832,ist24421,\n"
	    + "8421,ist146506,ist24421,\n" + "8422,ist20225,ist24518,\n" + "8422,ist23704,ist20225,\n"
	    + "8422,ist22159,ist20225,\n" + "8422,ist21534,ist20225,\n" + "8430,ist24439,ist12048,\n"
	    + "8431,ist24616,ist24439,\n" + "8431,ist152304,ist24439,\n" + "8431,ist24506,ist24439,\n"
	    + "8431,ist148357,ist24439,\n" + "8431,ist149608,ist24439,\n" + "8511,ist24925,ist12316,\n"
	    + "8511,ist147523,ist12316,\n" + "8610,ist23012,ist12451,\n" + "8611,ist23014,ist23012,\n"
	    + "8611,ist22230,ist23012,\n" + "8611,ist23848,ist23012,\n" + "8611,ist23069,ist23012,\n"
	    + "8611,ist23572,ist23012,\n" + "8611,ist90138,ist23012,\n" + "8611,ist25048,ist23012,\n"
	    + "8611,ist24313,ist23012,\n" + "8612,ist23716,ist23918,\n" + "8612,ist23918,ist23012,\n"
	    + "8613,ist23462,ist23012,\n" + "8613,ist23008,ist23012,\n" + "8613,ist23874,ist23012,\n"
	    + "3301,ist22769,ist11177,\n" + "3301,ist22235,ist11432,\n" + "3301,ist22653,ist12662,\n"
	    + "1144,ist24510,ist12931,\n" + "1144,ist24372,ist12662,\n" + "1144,ist46848,ist12662,\n"
	    + "1144,ist25056,ist12662,\n" + "2301,ist129875,ist12556,\n" + "2301,ist20927,ist11938,\n"
	    + "2301,ist20979,ist11988,\n" + "2301,ist21489,ist12061,\n" + "2301,ist21526,ist13175,\n"
	    + "2301,ist22651,ist13287,\n" + "2301,ist22777,ist11988,\n" + "2301,ist22982,ist129875,\n"
	    + "2301,ist23016,ist12556,\n" + "2301,ist23018,ist129875,\n" + "2301,ist23311,ist12081,\n"
	    + "2301,ist23627,ist11988,\n" + "2301,ist23849,ist12061,\n" + "2301,ist23884,ist11938,\n"
	    + "2301,ist24254,ist12729,\n" + "2301,ist31441,ist12051,\n" + "2301,ist32955,ist13296,\n"
	    + "2320,ist22988,ist12919,\n" + "2320,ist23344,ist12919,\n" + "2320,ist23920,ist12919,\n"
	    + "1604,ist24238,ist12144,\n" + "2001,ist23651,ist23308,\n" + "2001,ist23654,ist23308,\n"
	    + "2001,ist23655,ist23308,\n" + "2001,ist23685,ist23308,\n" + "2001,ist22996,ist23308,\n"
	    + "2001,ist23056,ist11387,\n" + "2001,ist23745,ist12736,\n" + "2001,ist23879,ist12325,\n"
	    + "2001,ist24413,ist12764,\n" + "2001,ist23308,ist12841,\n" + "1143,ist90144,ist12180,\n"
	    + "1608,ist24338,ist12299,\n" + "2003,ist23468,ist14020,\n" + "2003,ist23041,ist12494,\n"
	    + "2003,ist23040,ist12494,\n" + "2009,ist23054,ist12325,\n" + "2009,ist23877,ist11624,\n"
	    + "2009,ist24789,ist11624,\n" + "2010,ist23055,ist13977,\n" + "2012,ist23929,ist13428,\n"
	    + "2013,ist23471,ist12284,\n" + "2013,ist22787,ist12284,\n" + "2013,ist22776,ist12065,\n"
	    + "2013,ist24863,ist12284,\n" + "2013,ist23964,ist12284,\n" + "2014,ist22989,ist11385,\n"
	    + "2014,ist23051,ist11385,\n" + "2015,ist22645,ist12180,\n" + "2015,ist23062,ist12180,\n"
	    + "2015,ist21295,ist12180,\n" + "2015,ist23061,ist12180,\n" + "2015,ist20941,ist12180,\n"
	    + "2015,ist20939,ist12180,\n" + "2020,ist22357,ist12305,\n" + "2020,ist24335,ist12305,\n"
	    + "2040,ist24211,ist24757,\n" + "2040,ist24757,ist13084,\n" + "2040,ist24259,ist24757,\n"
	    + "2040,ist25181,ist25123,\n" + "2101,ist21251,ist11900,\n" + "2101,ist22690,ist11900,\n"
	    + "2101,ist22678,ist21561,\n" + "2101,ist23037,ist21561,\n" + "2101,ist23154,ist21561,\n"
	    + "2101,ist23696,ist21561,\n" + "2101,ist23800,ist21561,\n" + "2101,ist23808,ist21561,\n"
	    + "2101,ist23695,ist22690,\n" + "2101,ist23822,ist22690,\n" + "2101,ist23005,ist22690,\n"
	    + "2110,ist24886,ist11453,\n" + "1601,ist23031,ist11412,\n" + "1601,ist24542,ist11412,\n"
	    + "1601,ist24543,ist11412,\n" + "1601,ist23850,ist11814,\n" + "1601,ist146492,ist11814,\n"
	    + "1601,ist90241,ist30621,\n" + "2111,ist22148,ist14039,\n" + "2111,ist22504,ist14039,\n"
	    + "2112,ist22512,ist11049,\n" + "2112,ist22992,ist11049,\n" + "2112,ist23033,ist11049,\n"
	    + "2112,ist23815,ist11049,\n" + "2112,ist22899,ist11049,\n" + "2113,ist21515,ist11128,\n"
	    + "2113,ist22852,ist11128,\n" + "2113,ist22780,ist11128,\n" + "2113,ist24310,ist11128,\n"
	    + "2114,ist22660,ist12116,\n" + "2114,ist22475,ist12116,\n" + "2115,ist21561,ist11900,\n"
	    + "2115,ist23464,ist12403,\n" + "2115,ist23881,ist12403,\n" + "2120,ist22513,ist13145,\n"
	    + "2120,ist22657,ist13145,\n" + "2120,ist23717,ist13145,\n" + "2901,ist23074,ist13156,\n"
	    + "2901,ist24422,ist13156,\n" + "2901,ist24993,ist13156,\n" + "2801,ist24303,ist13085,\n"
	    + "2801,ist22652,ist13085,\n" + "2801,ist21708,ist13085,\n" + "2801,ist22342,ist13085,\n"
	    + "2801,ist22984,ist13085,\n" + "2801,ist23023,ist13085,\n" + "2801,ist23831,ist13085,\n"
	    + "2801,ist23837,ist13085,\n" + "2801,ist24386,ist13085,\n" + "2801,ist23565,ist13085,\n"
	    + "2801,ist24302,ist13085,\n" + "2801,ist146848,ist13085,\n" + "2201,ist23015,ist12272,\n"
	    + "2201,ist23656,ist12272,\n" + "2201,ist23705,ist11440,\n" + "2201,ist20933,ist11440,\n"
	    + "2201,ist23219,ist12272,\n" + "2201,ist23813,ist12855,\n" + "2201,ist23552,ist12272,\n"
	    + "2201,ist23746,ist12272,\n" + "2201,ist22684,ist12272,\n" + "1133,ist23825,ist11869,\n"
	    + "1137,ist24866,ist12361,\n" + "1137,ist32802,ist12361,\n" + "1137,ist33284,ist12361,\n"
	    + "1602,ist23940,ist11716,\n" + "1602,ist24246,ist11716,\n" + "1602,ist22524,ist11716,\n"
	    + "1602,ist24633,ist11388,\n" + "1602,ist24186,ist11716,\n" + "1602,ist24951,ist11716,\n"
	    + "1602,ist45689,ist11668,\n" + "2210,ist21919,ist11668,\n" + "2210,ist22331,ist11668,\n"
	    + "2210,ist23726,ist11668,\n" + "2211,ist22242,ist11388,\n" + "2211,ist22683,ist11388,\n"
	    + "2211,ist22739,ist11388,\n" + "2211,ist22491,ist11388,\n" + "2212,ist22991,ist13986,\n"
	    + "2212,ist23551,ist13986,\n" + "2212,ist24794,ist13986,\n" + "2213,ist23631,ist11716,\n"
	    + "2213,ist23757,ist11716,\n" + "2214,ist20943,ist11440,\n" + "2214,ist24249,ist11440,\n"
	    + "2215,ist22966,ist13722,\n" + "2215,ist23066,ist13722,\n" + "2215,ist23669,ist13722,\n"
	    + "2215,ist24502,ist13722,\n" + "2216,ist23473,ist11668,\n" + "2216,ist23030,ist11668,\n"
	    + "2216,ist23065,ist11668,\n" + "2216,ist23660,ist11668,\n" + "2217,ist23034,ist11869,\n"
	    + "2217,ist23846,ist11869,\n" + "2217,ist23706,ist11869,\n" + "2217,ist23858,ist11869,\n"
	    + "2220,ist21460,ist12093,\n" + "2220,ist23071,ist12093,\n" + "2220,ist23922,ist12093,\n"
	    + "2401,ist22974,ist11945,\n" + "2401,ist23017,ist11945,\n" + "2401,ist23911,ist11945,\n"
	    + "2401,ist22675,ist11945,\n" + "2401,ist22983,ist11945,\n" + "2401,ist23010,ist11945,\n"
	    + "2401,ist23206,ist11945,\n" + "2401,ist23309,ist11945,\n" + "2401,ist23024,ist11945,\n"
	    + "2401,ist23836,ist11945,\n" + "2401,ist22648,ist11945,\n" + "2420,ist22977,ist11945,\n"
	    + "2420,ist22511,ist11945,\n" + "2420,ist23668,ist11945,\n" + "2501,ist24398,ist12530,\n"
	    + "2501,ist23032,ist12530,\n" + "2501,ist23809,ist12530,\n" + "2501,ist21530,ist12530,\n"
	    + "2501,ist23046,ist12530,\n" + "2501,ist23625,ist12530,\n" + "2501,ist24644,ist12530,\n"
	    + "2501,ist22474,ist12530,\n" + "2501,ist21510,ist12530,\n" + "2501,ist22689,ist12530,\n"
	    + "2501,ist24898,ist12530,\n" + "2501,ist24271,ist12530,\n" + "2501,ist24793,ist12530,\n"
	    + "1145,ist23007,ist11063,\n" + "1145,ist23043,ist11063,\n" + "1145,ist23067,ist11063,\n"
	    + "1145,ist23070,ist11063,\n" + "1145,ist23380,ist11063,\n" + "1145,ist23480,ist11063,\n"
	    + "1145,ist23823,ist11063,\n" + "1145,ist23838,ist11063,\n" + "1145,ist23878,ist11063,\n"
	    + "1145,ist23885,ist11063,\n" + "1145,ist24637,ist11063,\n" + "1145,ist25005,ist11063,\n"
	    + "1145,ist23482,ist12916,\n" + "1145,ist148177,ist12916,\n" + "1145,ist23563,ist13387,\n"
	    + "1145,ist23691,ist13387,\n" + "1145,ist24918,ist13387,\n" + "1145,ist45840,ist13387,\n"
	    + "1145,ist23842,ist11011,\n" + "1145,ist23853,ist11011,\n" + "1145,ist23899,ist11011,\n"
	    + "1145,ist23817,ist23844,\n" + "1145,ist23871,ist23844,\n" + "1145,ist23295,ist11579,\n"
	    + "1145,ist23562,ist10845,\n" + "1145,ist130867,ist24515,\n" + "1111,ist23600,ist23594,\n"
	    + "1111,ist23617,ist23594,\n" + "1111,ist23594,ist10984,\n" + "1111,ist90358,ist12919,\n"
	    + "1113,ist24268,ist11938,\n" + "1115,ist23566,ist11165,\n" + "1115,ist23743,ist11165,\n"
	    + "1116,ist23576,ist10800,\n" + "8050,ist24147,ist11648,\n" + "8050,ist24444,ist11648,\n"
	    + "8050,ist24446,ist11648,\n" + "8050,ist24455,ist11648,\n" + "8050,ist24458,ist11648,\n"
	    + "8051,ist23868,ist11648,\n" + "8051,ist24447,ist23868,\n" + "8051,ist24448,ist23868,\n"
	    + "8052,ist22778,ist11648,\n" + "8052,ist24453,ist22778,\n" + "8053,ist23835,ist11648,\n"
	    + "8053,ist24431,ist23835,\n" + "8053,ist24445,ist23835,\n" + "8053,ist24499,ist23835,\n"
	    + "8054,ist23872,ist11648,\n" + "8054,ist24456,ist23872,\n" + "8055,ist23820,ist11648,\n"
	    + "8055,ist23900,ist11648,\n" + "8055,ist24451,ist11648,\n" + "8055,ist24148,ist11648,\n"
	    + "8055,ist24452,ist11648,\n" + "8056,ist23870,ist11648,\n" + "4015,ist23354,ist11787,\n"
	    + "8001,ist24196,ist22353,1\n" + "8002,ist90225,ist23541,1\n" + "8002,ist25177,ist23541,1\n"
	    + "8002,ist25176,ist23541,1\n" + "8030,ist31597,ist23827,1\n" + "8040,ist24586,ist25186,1\n"
	    + "8040,ist148783,ist25186,1\n" + "8040,ist24098,ist25186,1\n" + "8040,ist25138,ist25186,1\n"
	    + "8040,ist25146,ist25186,1\n" + "7610,ist25115,ist12922,1\n" + "7630,ist24660,ist11086,1\n"
	    + "7630,ist24606,ist11086,1\n" + "7630,ist24670,ist11086,1\n" + "7630,ist25015,ist11086,1\n"
	    + "7630,ist24332,ist11086,1\n" + "7640,ist24575,ist23821,1\n" + "6211,ist24270,ist23674,1\n"
	    + "6212,ist24061,ist22337,1\n" + "6212,ist10482803,ist22337,1\n" + "6213,ist24975,ist23223,1\n"
	    + "6213,ist24099,ist23223,1\n" + "6213,ist24187,ist23223,1\n" + "6215,ist24379,ist24661,1\n"
	    + "6215,ist24956,ist24661,1\n" + "6215,ist24988,ist24661,1\n" + "6215,ist24989,ist24661,1\n"
	    + "6215,ist90394,ist24661,1\n" + "6215,ist24075,ist24661,1\n" + "6215,ist25164,ist24661,1\n"
	    + "6223,ist90111,ist23570,1\n" + "6224,ist24120,ist21903,1\n" + "6224,ist24964,ist21903,1\n"
	    + "6224,ist24966,ist21903,1\n" + "6224,ist24450,ist21903,1\n" + "6241,ist25187,ist24588,1\n"
	    + "6241,ist24724,ist24588,1\n" + "6242,ist24960,ist25185,1\n" + "6242,ist24645,ist25185,1\n"
	    + "6242,ist25130,ist25185,1\n" + "6242,ist25145,ist25185,1\n" + "6243,ist24062,ist24588,1\n"
	    + "6243,ist24389,ist24588,1\n" + "6243,ist141687,ist13085,1\n" + "6243,ist24980,ist24588,1\n"
	    + "6401,ist24875,ist21846,1\n" + "6401,ist25096,ist24877,1\n" + "6401,ist25136,ist24877,1\n"
	    + "6410,ist90385,ist23932,1\n" + "6412,ist24064,ist22752,1\n" + "6413,ist24688,ist22751,1\n"
	    + "6413,ist24726,ist22751,1\n" + "6420,ist32949,ist23932,1\n" + "6301,ist24979,ist23470,1\n"
	    + "6301,ist24981,ist23470,1\n" + "6312,ist24720,ist22234,1\n" + "6313,ist90498,ist23555,1\n"
	    + "6315,ist24755,ist10482756,1\n" + "6315,ist24613,ist10482756,1\n" + "6315,ist24508,ist10482756,1\n"
	    + "6315,ist24894,ist10482756,1\n" + "6315,ist25068,ist10482756,1\n" + "6316,ist24614,ist23711,1\n"
	    + "6316,ist24615,ist23711,1\n" + "6316,ist24509,ist23711,1\n" + "6316,ist129970,ist23711,1\n"
	    + "6332,ist22708,ist10482686,1\n" + "6332,ist24321,ist10482686,1\n" + "6332,ist25037,ist10482686,1\n"
	    + "6331,ist25038,ist10482686,1\n" + "6332,ist25041,ist10482686,1\n" + "6332,ist25072,ist10482686,1\n"
	    + "6332,ist25153,ist10482686,1\n" + "6333,ist24685,ist23057,1\n" + "6333,ist25024,ist23057,1\n"
	    + "6333,ist25039,ist23057,1\n" + "6333,ist25055,ist23057,1\n" + "6333,ist25079,ist23057,1\n"
	    + "6333,ist25095,ist23057,1\n" + "6333,ist25110,ist23057,1\n" + "6333,ist25112,ist23057,1\n"
	    + "8311,ist24293,ist23978,1\n" + "8311,ist24280,ist23978,1\n" + "8312,ist24883,ist23703,1\n"
	    + "8312,ist24396,ist23703,1\n" + "8411,ist143645,ist24459,1\n" + "8411,ist24727,ist24459,1\n"
	    + "8411,ist24869,ist24459,1\n" + "8411,ist24983,ist24459,1\n" + "8411,ist25125,ist24459,1\n"
	    + "8412,ist23490,ist23000,1\n" + "8412,ist24908,ist23000,1\n" + "8421,ist46082,ist24421,1\n"
	    + "8431,ist154457,ist24439,1\n" + "8431,ist153877,ist24439,1\n" + "8510,ist142020,ist12316,1\n"
	    + "8512,ist90411,ist12316,1\n" + "8611,ist90410,ist23012,1\n" + "8612,ist24044,ist23918,1\n"
	    + "8612,ist24247,ist23918,1\n" + "8612,ist24054,ist23918,1\n" + "3301,ist24967,ist12931,1\n"
	    + "1144,ist25134,ist12662,1\n" + "2301,ist24300,ist12268,1\n" + "2320,ist24079,ist12919,1\n"
	    + "2320,ist24127,ist12919,1\n" + "1604,ist24971,ist12144,1\n" + "2001,ist24697,ist24597,1\n"
	    + "2001,ist24076,ist23308,1\n" + "2001,ist24119,ist23308,1\n" + "2001,ist24702,ist23308,1\n"
	    + "2001,ist24739,ist23308,1\n" + "2001,ist24212,ist24757,1\n" + "2001,ist25162,ist25123,1\n"
	    + "1102,ist24992,ist24577,1\n" + "1126,ist24058,ist11400,1\n" + "1126,ist14344,ist11400,1\n"
	    + "1126,ist25169,ist11624,1\n" + "1608,ist24033,ist12065,1\n" + "2009,ist24533,ist11624,1\n"
	    + "2011,ist24640,ist12448,1\n" + "2015,ist25020,ist12180,1\n" + "2015,ist164270,ist12180,1\n"
	    + "2020,ist24716,ist12305,1\n" + "2040,ist24844,ist24757,1\n" + "2040,ist25166,ist24757,1\n"
	    + "2040,ist24278,ist24757,1\n" + "2110,ist24273,ist11453,1\n" + "1601,ist24626,ist12760,1\n"
	    + "2115,ist136657,ist12403,1\n" + "1134,ist25104,ist11029,1\n" + "2801,ist24537,ist13085,1\n"
	    + "2801,ist24322,ist13085,1\n" + "2801,ist24330,ist13085,1\n" + "2201,ist24260,ist12272,1\n"
	    + "1137,ist145517,ist12361,1\n" + "1137,ist32386,ist12361,1\n" + "1137,ist25184,ist12361,1\n"
	    + "1145,ist24206,ist11063,1\n" + "1145,ist149536,ist11063,1\n" + "1145,ist25182,ist11063,1\n"
	    + "1145,ist24658,ist23844,1\n" + "1145,ist24668,ist23844,1\n" + "1145,ist25132,ist23913,1\n"
	    + "8050,ist24507,ist11648,1\n" + "8050,ist25025,ist11648,1\n" + "8050,ist25111,ist11648,1\n"
	    + "8051,ist24435,ist23868,1\n" + "8052,ist24426,ist22778,1\n" + "8052,ist24430,ist22778,1\n"
	    + "8052,ist24438,ist22778,1\n" + "8052,ist24553,ist22778,1\n" + "8052,ist24733,ist22778,1\n"
	    + "8052,ist24734,ist22778,1\n" + "8052,ist24743,ist22778,1\n" + "8052,ist24784,ist22778,1\n"
	    + "8052,ist24787,ist22778,1\n" + "8052,ist25118,ist22778,1\n" + "8053,ist163826,ist23835,1\n"
	    + "8053,ist24428,ist23835,1\n" + "8054,ist24785,ist23872,1\n" + "8054,ist25026,ist23872,1\n"
	    + "8054,ist24427,ist23872,1\n" + "8054,ist24429,ist23872,1\n" + "8055,ist24432,ist11648,1\n"
	    + "8055,ist24433,ist11648,1\n" + "8055,ist24732,ist11648,1\n" + "8055,ist151317,ist11648,1\n"
	    + "8055,ist24848,ist11648,1\n" + "8055,ist24881,ist11648,1\n");

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
		//let's also add each line to the originalRelationList
		originalRelationsList.add(strLine);
	    }
	    // Close the input stream
	    //	    in.close();
	} catch (IOException e) {// Catch exception if any
	    out.println("Error: " + e.getMessage());
	}

	out.println(avaliators.size());

	for (Integer costCenter : avaliators.keySet()) {
	    ProcessCostCenter proccessCostCenter = new ProcessCostCenter(costCenter, avaliators.get(costCenter), responsibles,
		    out, costCentersMartelados);
	    proccessCostCenter.start();
	    try {
		proccessCostCenter.join();
	    } catch (InterruptedException e) {
		throw new Error(e);
	    }

	}

	migrateStuff();

	//let's verify the data
	validateData();

    }

    public final static Comparator<String> ORDER_BY_EVALUATED_IST_ID = new Comparator<String>() {

	@Override
	public int compare(String o1, String o2) {
	    // TODO Auto-generated method stub
	    return 0;
	}
    };

    private void validateData() {
	//let's get the data from the system into the input format
	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	out.println("Retrieving statistics for SIADAP for year: " + today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();

	List<Accountability> allAccountabilities = new ArrayList<Accountability>();

	allAccountabilities.addAll(evaluationRelation.getAccountabilities());

	//going through all of the evaluation relations that are active now

	HashSet<User> evaluators = new HashSet<User>();

	for (Accountability accountability : allAccountabilities) {
	    if (accountability.isActiveNow()) {
		//now, lets get all of the evaluators
		if (accountability.getParent() instanceof Person) {
		    evaluators.add(((Person) accountability.getParent()).getUser());
		}

		else if (accountability.getParent() instanceof Unit) {
		    evaluators.add(((Person) accountability.getChild()).getUser());
		}
	    }
	}

	//now let's iterate through each of the evaluators and get all of the persons that are evaluated

	for (User user : evaluators) {
	    PersonSiadapWrapper evaluatorSiadapWrapper = new PersonSiadapWrapper(user.getPerson(), today.getYear());
	    for (PersonSiadapWrapper evaluatedPerson : evaluatorSiadapWrapper.getPeopleToEvaluate()) {
		//let's get his cost center and write the line out for each person
		String stringToWrite = evaluatedPerson.getWorkingUnit().getUnit().getAcronym().substring(4) + ","
			+ evaluatedPerson.getPerson().getUser().getUsername() + ","
			+ evaluatorSiadapWrapper.getPerson().getUser().getUsername() + ",";
		if (!evaluatedPerson.isQuotaAware()) {
		    stringToWrite = stringToWrite.concat("1");
		}
		systemsRelationsList.add(stringToWrite);
	    }
	}

	//sort both lists
	Collections.sort(originalRelationsList);
	Collections.sort(systemsRelationsList);

	out.println("original relations list matches systemRelationsList? " + originalRelationsList.equals(systemsRelationsList));

	out.println("original relations list number of lines: " + originalRelationsList.size());
	out.println("system relations list number of lines: " + systemsRelationsList.size());

	debug("printing the original relations list");
	for (String string : originalRelationsList) {
	    debug(string);
	}
	debug("--\n\n");
	debug("printing the system's relations list");
	for (String string : systemsRelationsList) {
	    debug(string);
	}

	Diff differences = new Diff(originalRelationsList.toArray());
	try {
	    Revision rev = differences.diff(systemsRelationsList.toArray());
	    out.println(rev.toRCSString());
	    out.println("rev to string: " + rev.toString());
	} catch (DifferentiationFailedException e) {
	    e.printStackTrace(out);
	}


    }

    public static class ProcessWorkers extends TransactionalThread {
	Map<Integer, List<Evaluator>> avaliators;
	Integer costCenter;
	ArrayList<String> costCentersMartelados;
	PrintWriter out;

	public ProcessWorkers(Integer costCenter, Map<Integer, List<Evaluator>> avaliators,
		ArrayList<String> costCentersMartelados, PrintWriter out) {
	    this.costCenter = costCenter;
	    this.avaliators = avaliators;
	    this.costCentersMartelados = costCentersMartelados;
	    this.out = out;
	}

	@Override
	public void transactionalRun() {
	    final LocalDate today = new LocalDate();
	    final LocalDate startOfTheYear = new LocalDate(today.getYear(), 1, 1);
	    final LocalDate endOfTheYear = new LocalDate(today.getYear(), 12, 31);
	    SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	    final AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	    AccountabilityType workingRelation = configuration.getWorkingRelation();
	    AccountabilityType workingRelationWithNoQuota = configuration.getWorkingRelationWithNoQuota();
	    PartyPredicate evalForYearPredicate = new AccTypeForGivenYearClassPredicate(evaluationRelation, today, null);
	    List<Evaluator> evaluators = avaliators.get(costCenter);
	    String centerString = costCenter.toString();
	    if (evaluationRelation == null)
		throw new Error("Holy crap!");
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    c = validateCCenter(centerString, c, costCentersMartelados);
	    Unit unit = c.getUnit();

	    for (Evaluator evaluator : evaluators) {
		Person evaluatorPerson = evaluator.getUser().getPerson();
		if (evaluatorPerson == null) {
		    out.println("WTF: " + evaluator.getUser().getUsername());
		}
		if (evaluatorPerson.getParentUnits(evaluationRelation) == null) {
		    out.println("WTF-relation for: " + evaluator.getUser().getUsername());
		}

		PartyPredicate parentEvalUnitsCurrentDate = new AccTypeForGivenYearClassPredicate(evaluationRelation, today,
			Unit.class);

		boolean isResponsible = evaluatorPerson.getParents(parentEvalUnitsCurrentDate).contains(unit);
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

			if (inDevelopmentSystem) {

			    Person p = Person.create(MultiLanguageString.i18n().add("pt", "DUMMY USER").finish(),
				    Person.getPartyTypeInstance());
			    p.setUser(user);
			} else {

			    LoginListner.importUserInformation(evaluated.istId);
			}
			evaluatedPerson = user.getPerson();
			if (evaluatedPerson == null)
			    throw new Error("Error, creation of person for user " + evaluated.istId + " didn't succeeded");
		    }

		    PartyPredicate workingPartyPredicate = new AccTypeForGivenYearClassPredicate(workingRelation, today, null);
		    PartyPredicate workingNoQuotaPartyPredicate = new AccTypeForGivenYearClassPredicate(
			    workingRelationWithNoQuota, today, null);

		    for (Accountability acc : evaluatedPerson.getParentAccountabilities()) {
			if (acc.getParent() != unit
				&& (workingPartyPredicate.eval(null, acc) || workingNoQuotaPartyPredicate.eval(null, acc))
				&& acc.getParent() instanceof Unit) {
			    //remove
			    debug("Going to remove previous working relation for " + acc.getChild().getPartyName() + " "
				    + acc.getDetailsString() + " from unit " + acc.getParent().getPresentationName(), out);
			    evaluatedPerson.removeParent(acc);
			}
		    }

		    if (!adist && !unit.getChildren(workingPartyPredicate).contains(evaluatedPerson)) {
			evaluatedPerson.addParent(unit, workingRelation, minParentExistence(unit, startOfTheYear), endOfTheYear);
		    }
		    if (adist && !unit.getChildren(workingNoQuotaPartyPredicate).contains(evaluatedPerson)) {
			evaluatedPerson.addParent(unit, workingRelationWithNoQuota, minParentExistence(unit, startOfTheYear),
				endOfTheYear);
		    }
		    //let's remove any custom eval relations that might be present for this evaluatedPerson
		    for (Accountability acc : evaluatedPerson.getParentAccountabilities()) {
			    if (!acc.getParent().equals(evaluatorPerson) && evalForYearPredicate.eval(evaluatedPerson, acc)
				    && acc.getChild() instanceof Person && acc.getParent() instanceof Person) {
				//remove
				debug("Going to remove previous personal eval rel for " + acc.getChild().getPartyName() + " "
					+ acc.getDetailsString() + " with unit " + acc.getParent().getPresentationName(), out);
				evaluatedPerson.removeParent(acc);
			    }
			}
		    if (!isResponsible) {

			if (!evaluatorPerson.getChildren(evalForYearPredicate).contains(evaluatedPerson)) {
			    evaluatedPerson.addParent(evaluatorPerson, evaluationRelation,
				    minParentExistence(evaluatorPerson, startOfTheYear), endOfTheYear);
			}
		    }
		}
	    }

	}

    }

    public static LocalDate minParentExistence(final Party party, final LocalDate localDate) {
	LocalDate min = null;
	for (final Accountability accountability : party.getParentAccountabilitiesSet()) {
	    final LocalDate beginDate = accountability.getBeginDate();
	    if (min == null || min.isAfter(beginDate)) {
		min = beginDate;
	    }
	}
	if (min == null) {
	    throw new Error("????");
	}
	return min.isBefore(localDate) ? localDate : min;
    }

    public static class ProcessResponsibles extends TransactionalThread {

	List<Responsible> responsibles;
	PrintWriter out;

	public ProcessResponsibles(List<Responsible> responsibles, PrintWriter out) {
	    this.responsibles = responsibles;
	    this.out = out;
	}

	@Override
	public void transactionalRun() {
	    final LocalDate today = new LocalDate();
	    final LocalDate startOfTheYear = new LocalDate(today.getYear(), 1, 1);
	    final LocalDate endOfTheYear = new LocalDate(today.getYear(), 12, 31);
	    SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	    final AccountabilityType evaluationRelation = configuration.getEvaluationRelation();

	    PartyPredicate evalForYearPredicate = new AccTypeForGivenYearClassPredicate(evaluationRelation, today, null);
	    // TODO Auto-generated method stub
	    for (Responsible responsible : responsibles) {
		Unit unit = responsible.getCenter().getUnit();
		Person person = responsible.getUser().getPerson();
		//let's remove the previous responsibles
		for (Accountability acc : unit.getChildAccountabilities()) {
		    if (acc.getChild() != person && evalForYearPredicate.eval(acc.getChild(), acc)) {
			debug("Going to remove previous responsible " + acc.getChild().getPartyName() + " "
				+ acc.getDetailsString() + " from unit " + unit.getPresentationName(), out);
			acc.getChild().removeParent(acc);
		    }
		}
		if (!unit.getChildren(evalForYearPredicate).contains(person)) {
		    LocalDate startDate = minParentExistence(unit, startOfTheYear);
		    person.addParent(unit, evaluationRelation, startDate, endOfTheYear);
		    debug("Really added the responsible " + responsible.getUser().getUsername() + " to cc "
			    + responsible.getCenter().getCostCenter() + " starting at " + startDate + " ending at "
			    + endOfTheYear, out);
		}
	    }

	}
    }

    public static class ProcessCostCenter extends TransactionalThread {
	Integer costCenterBeingProcessed;
	List<Evaluator> listEvaluators;
	List<Responsible> responsibles;
	PrintWriter out;
	ArrayList<String> costCentersMartelados;

	public ProcessCostCenter(Integer processCostCenter, List<Evaluator> listEvaluators, List<Responsible> responsibles,
		PrintWriter out, ArrayList<String> costCentersMartelados) {
	    this.costCenterBeingProcessed = processCostCenter;
	    this.listEvaluators = listEvaluators;
	    this.responsibles = responsibles;
	    this.out = out;
	    this.costCentersMartelados = costCentersMartelados;
	}

	@Override
	public void transactionalRun() {
	    List<Evaluator> evaluators = listEvaluators;
	    String centerString = costCenterBeingProcessed.toString();
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    c = validateCCenter(centerString, c, costCentersMartelados);
	    Person responsible = null;
	    int max = 0;
	    for (Evaluator evaluator : evaluators) {
		User user = User.findByUsername(evaluator.istId);
		if (user == null) {
		    user = User.createNewUser(evaluator.istId);
		    out.println("Created user: " + evaluator.istId);
		}
		if (user.getPerson() == null) {
		    if (inDevelopmentSystem) {

			Person p = Person.create(MultiLanguageString.i18n().add("pt", "DUMMY USER").finish(),
				Person.getPartyTypeInstance());
			p.setUser(user);
		    } else {
			LoginListner.importUserInformation(evaluator.istId);

		    }

		}
		if (evaluator.size() > max) {
		    responsible = user.getPerson();
		    max = evaluator.size();
		}
	    }
	    debug("Added responsible for cc " + c.getCostCenter() + " the " + responsible.getUser().getUsername(), out);
	    responsibles.add(new Responsible(responsible.getUser(), c));
	}

    }

    //    static protected void addMappedUserByUsername(String username) {
    //
    //	User user = User.findByUsername(username);
    //
    //	if (user == null) {
    //		user = User.createNewUser(username);
    //	    	out.println("Created user: " + username);
    //	} else
    //	    mappedUsers.add(user);
    //    }

    static protected CostCenter validateCCenter(String centerString, CostCenter c, ArrayList<String> costCentersMartelados)
	    throws Error {
	if (c == null) {
	    Integer cc = Integer.valueOf(centerString);
	    String newCenterString = String.valueOf(cc.intValue() + 1);
	    c = (CostCenter) CostCenter.findUnitByCostCenter(newCenterString);
	    if (c == null)
		throw new Error("Cost center " + centerString + " wasn't found");
	    else {

		costCentersMartelados.add("Cost center " + centerString + " wasn't found but " + newCenterString + " was");

	    }
	}
	return c;
    }

    static class AccTypeForGivenYearClassPredicate extends PartyPredicate {
	AccountabilityType evaluationRelation;
	LocalDate date;
	Class<? extends Party> typeToUse;

	public AccTypeForGivenYearClassPredicate(AccountabilityType evaluationRelation, LocalDate date,
		Class<? extends Party> typeToUse) {
	    this.evaluationRelation = evaluationRelation;
	    this.date = date;
	    this.typeToUse = typeToUse;
	}

	@Override
	public boolean eval(Party party, Accountability accountability) {
	    AccountabilityType accType = accountability.getAccountabilityType();
	    return (hasClass(typeToUse, party) && accType == evaluationRelation && accountability.isActive(date));
	}

	@Override
	protected boolean hasClass(final Class<? extends Party> clazz, final Party party) {
	    return clazz == null || clazz.isAssignableFrom(party.getClass());
	}

    }

    private void migrateStuff() {
	ProcessResponsibles processResponsibles = new ProcessResponsibles(responsibles, out);

	processResponsibles.start();
	try {
	    processResponsibles.join();
	} catch (InterruptedException e) {
	    throw new Error(e);
	}

	for (Integer costCenter : avaliators.keySet()) {
	    ProcessWorkers processWorkers = new ProcessWorkers(costCenter, avaliators, costCentersMartelados, out);
	    processWorkers.start();
	    try {
		processWorkers.join();
	    } catch (InterruptedException e) {
		throw new Error(e);
	    }

	}
	out.println("DONE importing the list!");
	for (String string : costCentersMartelados) {
	    out.println(string);
	}

    }

    private static void debug(String message, PrintWriter out) {

	if (debugModeOn)
	    out.println("Debug: " + message);

    }

    private void debug(String message) {

	if (debugModeOn)
	    out.println("Debug: " + message);

    }

    private void processLine(String strLine) {
	String[] values = strLine.split(",");

	if (values.length < 3 || values.length > 5) {
	    out.println("skipped: " + strLine);
	    return;
	}
	String cc = values[0].trim();
	Integer ccNumber = Integer.valueOf(cc);
	String evaluatedId = values[1].trim();
	String evaluatorId = values[2].trim();
	Boolean adist = values.length == 4 ? values[3].trim().equals("1") : false;

	if (StringUtils.isEmpty(evaluatorId)) {
	    return;
	}

	List<Evaluator> evaluators = avaliators.get(ccNumber);
	if (evaluators == null) {
	    debug("Starting evalutors for " + cc);
	    evaluators = new ArrayList<Evaluator>();
	    avaliators.put(ccNumber, evaluators);
	}
	boolean added = false;
	for (Evaluator evaluator : evaluators) {
	    if (evaluator.match(evaluatorId)) {
		added = true;
		debug("Adding existing evaluator " + evaluatorId + " for " + cc + "adist: " + adist);
		evaluator.addEvaluated(evaluatedId, adist);
	    }
	}
	if (!added) {
	    debug("New evaluator " + evaluatorId + " for " + cc + " adist: " + adist);
	    Evaluator evaluator = new Evaluator(evaluatorId);
	    evaluator.addEvaluated(evaluatedId, adist);
	    evaluators.add(evaluator);
	}
    }

    public static class Evaluator {
	String istId;
	List<Evaluated> evaluated;

	public Evaluator(String istId) {
	    //	    addMappedUserByUsername(istId);
	    this.istId = istId.trim();
	    evaluated = new ArrayList<Evaluated>();
	}

	public void addEvaluated(String istId, Boolean adist) {
	    //	    addMappedUserByUsername(istId);
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
	    //	    addMappedUserByUsername(this.istId);
	}

	public boolean getAdist() {
	    return adist;
	}

	public User getUser() {
	    return User.findByUsername(istId);
	}
    }

    public static class Responsible {
	String userOId;
	String costCenterOId;

	public Responsible(User user, CostCenter center) {
	    //	    mappedUsers.add(user);
	    this.userOId = user.getExternalId();
	    this.costCenterOId = center.getExternalId();
	}

	public User getUser() {
	    return AbstractDomainObject.fromExternalId(userOId);
	}

	public CostCenter getCenter() {
	    return AbstractDomainObject.fromExternalId(costCenterOId);
	}

    }

}
