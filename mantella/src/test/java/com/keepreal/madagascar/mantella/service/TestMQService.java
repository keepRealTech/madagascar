package com.keepreal.madagascar.mantella.service;

import com.keepreal.madagascar.mantella.MantellaApplication;
import com.keepreal.madagascar.mantella.factory.TimelineFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-06-22
 **/

@SpringBootTest(classes = MantellaApplication.class)
@RunWith(SpringRunner.class)
public class TestMQService {

    @Autowired
    private FeedService feedService;
    @Autowired
    private TimelineService timelineService;
    @Autowired
    private TimelineFactory timelineFactory;
    @Autowired
    private IslandService islandService;
    private static final int TIMELINE_PULL_PAGESIZE = 1000;


    @Test
    public void test() {
        Map<String, List<String>> map = new HashMap<>();
        // select userList
        List<String> userIdList = getUserIdListTest();
        userIdList.forEach(id -> map.put(id, islandService.retrieveIslandIdListByUserId(id)));

        // <String, List<String>> map
        map.forEach((userId, islandList) -> {
            if (islandList.size() > 0) {
                islandList.forEach(islandId -> this.feedService.retrieveFeedsByIslandIdAndTimestampBefore(islandId,
                        System.currentTimeMillis(), TIMELINE_PULL_PAGESIZE)
                        .map(feed -> this.timelineFactory.valueOf(feed.getId(), feed.getIslandId(),
                                userId, feed.getCreatedAt(), "auto_input"))
                        .compose(this.timelineService::insertAll)
                        .blockLast());
            }
        });

    }

    private List<String> getUserIdList() {
        return Arrays.asList("6678224942909095936",
                "6678224942061850624",
                "6678220848672800768",
                "6678183501235224576",
                "6678173566178951168",
                "6678153822558224384",
                "6678141847656726528",
                "6678092442157191168",
                "6678092191820156928",
                "6678090388449792000",
                "6678090007057534976",
                "6678089444395843584",
                "6678088361934061568",
                "6678084346018009088",
                "6677933635355545600",
                "6677929405194440704",
                "6677881265040920576",
                "6677869456368074752",
                "6677861229190451200",
                "6677761092837445632",
                "6677748809059008512",
                "6677630805973405696",
                "6677559409700245504",
                "6677542729141977088",
                "6677517400188911616",
                "6677498396745400320",
                "6677490386060972032",
                "6677489151463395328",
                "6677486391712022528",
                "6677485658321195008",
                "6677483074244050944",
                "6677482096165916672",
                "6677481070276902912",
                "6677478615505965056",
                "6677477954819194880",
                "6677477428312408064",
                "6677476454218862592",
                "6677474915781705728",
                "6677474764438634496",
                "6677474537061224448",
                "6677474008482447360",
                "6677472084714586112",
                "6677471715548729344",
                "6677363218169466880",
                "6677276502943334400",
                "6677259492263989248",
                "6677174064735326208",
                "6677142667756834816",
                "6677108385256574976",
                "6677079049862381568",
                "6676867057390190592",
                "6676860914769068032",
                "6676832265458159616",
                "6676831648190824448",
                "6676810391969857536",
                "6676800444657831936",
                "6676798766239977472",
                "6676749878376267776",
                "6676699297829883904",
                "6676656120741761024",
                "6676483440042315776",
                "6676470323501596672",
                "6676413849358307328",
                "6676404603816247296",
                "6676398111423725568",
                "6676362263609212928",
                "6676350720582619136",
                "6676339401036926976",
                "6676321854992420864",
                "6676313267356303360",
                "6676310030758711296",
                "6676286684486701056",
                "6676273950005137408",
                "6676143904942718976",
                "6676130112737382400",
                "6676118068382404608",
                "6676113390928138240",
                "6676073060195373056",
                "6676055496899694592",
                "6676053675229577216",
                "6676034882398851072",
                "6676025207070658560",
                "6676022710398619648",
                "6676019041947095040",
                "6676014198268366848",
                "6676005612221894656",
                "6675969224789200896",
                "6675953034574434304",
                "6675952599243423744",
                "6675947182073319424",
                "6675792040460025856",
                "6675788130844409856",
                "6675787657252966400",
                "6675786049114865664",
                "6675768829605318656",
                "6675733710983135232",
                "6675732655197126656",
                "6675711383134277632",
                "6675697200439234560",
                "6675697129396113408",
                "6675679108300537856",
                "6675670082623897600",
                "6675669984951140352",
                "6675666230499606528",
                "6675659001587306496",
                "6675658786423705600",
                "6675656392247873536",
                "6675642547269795840",
                "6675639359594561536",
                "6675635899893551104",
                "6675633356052365312",
                "6675618651795619840",
                "6675601779322781696",
                "6675600676132753408",
                "6675591302333075456",
                "6675573772730241024",
                "6675573216469061632",
                "6675568672859230208",
                "6675567555479863296",
                "6675557870458634240",
                "6675555120031207424",
                "6675554591414685696",
                "6675541396893470720",
                "6675540097401622528",
                "6675499124281839616",
                "6675489278853648384",
                "6675454297792057344",
                "6675445473374896128",
                "6675437022791860224",
                "6675435452108898304",
                "6675429832647507968",
                "6675428577678196736",
                "6675428010214031360",
                "6675425132489478144",
                "6675421145207930880",
                "6675413084095385600",
                "6675411698297344000",
                "6675411322860994560",
                "6675402382127398912",
                "6675400412448690176",
                "6675399369149124608",
                "6675376588449644544",
                "6675374319306014720",
                "6675364104359641088",
                "6675363884410339328",
                "6675362589473505280",
                "6675358698765811712",
                "6675325550384386048",
                "6675323132233912320",
                "6675321345577844736",
                "6675307137977028608",
                "6675299017565405184",
                "6675298000983556096",
                "6675295660951994368",
                "6675294180983443456",
                "6675294005124665344",
                "6675278751388930048",
                "6675263824062316544",
                "6675248074773757952",
                "6675241522524323840",
                "6675237981197959168",
                "6675234213978968064",
                "6675203624856977408",
                "6675186540366270464",
                "6675185104706338816",
                "6675173593812303872",
                "6675156178298732544",
                "6675090417517592576",
                "6675085830551240704",
                "6675081618467586048",
                "6675080689227927552",
                "6675068331315171328",
                "6675050666940960768",
                "6675050180359753728",
                "6675047181218480128",
                "6675045369123966976",
                "6675042436684713984",
                "6675035834158157824",
                "6675029226728460288",
                "6675011085369610240",
                "6674990202894684160",
                "6674987380119703552",
                "6674981888584585216",
                "6674970136698425344",
                "6674965547567808512",
                "6674955374556090368",
                "6674949101601161216",
                "6674948509608710144",
                "6674939421063647232",
                "6674928436135329792",
                "6674919400753070080",
                "6674898375696973824",
                "6674885261484097536",
                "6674884363403919360",
                "6674883693514854400",
                "6674881198407290880",
                "6674879446861742080",
                "6674878648329175040",
                "6674872656702996480",
                "6674868300800856064",
                "6674867541719908352",
                "6674866478015057920",
                "6674866340169252864",
                "6674866102905864192",
                "6674865785866813440",
                "6674865674126364672",
                "6674862914219806720",
                "6674862123052437504",
                "6674860165184880640",
                "6674858662290259968",
                "6674858478252593152",
                "6674857079309598720",
                "6674821941515255808",
                "6674789851973287936",
                "6674736210612588544",
                "6674713948987592704",
                "6674705745323229184",
                "6674705056161333248",
                "6674704501879869440",
                "6674701506173730816",
                "6674698734342766592",
                "6674689342696329216",
                "6674687856805412864",
                "6674685194106048512",
                "6674681136355999744",
                "6674680471202299904",
                "6674677794586234880",
                "6674676289741258752",
                "6674674003585536000",
                "6674673545802420224",
                "6674668045836750848",
                "6674659383579774976",
                "6674658659110223872",
                "6674657996133371904",
                "6674656684595478528",
                "6674656327874117632",
                "6674654788958814208",
                "6674654350104596480",
                "6674651016530493440",
                "6674650713408147456",
                "6674647150346895360",
                "6674640527498936320",
                "6674638745242701824",
                "6674638221885833216",
                "6674629153649594368",
                "6674622168262770688",
                "6674618034142842880",
                "6674616152750034944",
                "6674614157846773760",
                "6674613410962870272",
                "6674612466313330688",
                "6674605153238126592",
                "6674600890302726144",
                "6674600395454545920",
                "6674598557095297024",
                "6674596238324666368",
                "6674594146696564736",
                "6674593508512235520",
                "6674592290276642816",
                "6674586629736235008",
                "6674585408954699776",
                "6674584523138662400",
                "6674583051961368576",
                "6674580607827181568",
                "6674580597781823488",
                "6674580235523981312",
                "6674579742357716992",
                "6674578697476902912",
                "6674577153318719488",
                "6674573704116047872",
                "6674566984211894272",
                "6674564929393004544",
                "6674564916529074176",
                "6674561515032412160",
                "6674559962837942272",
                "6674554099695878144",
                "6674550651298512896",
                "6674547652937383936",
                "6674547223428071424",
                "6674546545527885824",
                "6674545987685453824",
                "6674545737373585408",
                "6674545073033580544",
                "6674544701049143296",
                "6674543583430709248",
                "6674541156585443328",
                "6674534792739946496",
                "6674530009488556032",
                "6674528837801349120",
                "6674526251534782464",
                "6674524430598672384",
                "6674523100010250240",
                "6674521134047694848",
                "6674520391903346688",
                "6674518401261506560",
                "6674517288399409152",
                "6674517199496937472",
                "6674516303690076160",
                "6674516010575331328",
                "6674514179216375808",
                "6674512967121244160",
                "6674512114817695744",
                "6674507025516331008",
                "6674504211931070464",
                "6674501700239233024",
                "6674500892399501312",
                "6674498018340769792",
                "6674497821393027072",
                "6674495930600787968",
                "6674495409085218816",
                "6674493870832615424",
                "6674491413217939456",
                "6674491294879846400",
                "6674490939228033024",
                "6674487053406699520",
                "6674485519839137792",
                "6674483620041723904",
                "6674483531906809856",
                "6674482408990969856",
                "6674481677315608576",
                "6674478404391145472",
                "6674478148580544512",
                "6674477935434399744",
                "6674477422013845504",
                "6674476860262318080",
                "6674475425248313344",
                "6674475018228858880",
                "6674473379816280064",
                "6674472654587564032",
                "6674470609637867520",
                "6674469984413945856",
                "6674469467822493696",
                "6674469423962652672",
                "6674469104713203712",
                "6674468811598462976",
                "6674468049598287872",
                "6674466152808189952",
                "6674465977528221696",
                "6674465544449556480",
                "6674463675450920960",
                "6674462154373992448",
                "6674460852747239424",
                "6674460436949110784",
                "6674459577246810112",
                "6674458076193488896",
                "6674454439983583232",
                "6674453031947337728",
                "6674452676337471488",
                "6674451160935432192",
                "6674446213623840768",
                "6674446212935979008",
                "6674440843320889344",
                "6674440454647316480",
                "6674438772559446016",
                "6674438681127813120",
                "6674438389543997440",
                "6674429731909144576",
                "6674426481776525312",
                "6674420720216313856",
                "6674414368341360640",
                "6674413527949643776",
                "6674392500037226496",
                "6674387178891513856",
                "6674386016263995392",
                "6674380799824891904",
                "6674372159978082304",
                "6674364290994012160",
                "6674362744377970688",
                "6674358509317914624",
                "6674353917490364416",
                "6674352015105064960",
                "6674350707669209088",
                "6674346360256335872",
                "6674345936992342016",
                "6674345929702637568",
                "6674338661040918528",
                "6674336374977462272",
                "6674335362401173504",
                "6674335036801552384",
                "6674334108014870528",
                "6674332438979674112",
                "6674332031859560448",
                "6674331954030055424",
                "6674331149088587776",
                "6674330887997362176",
                "6674330534878908416",
                "6674328194440495104",
                "6674326737372844032",
                "6674326468933193728",
                "6674326170873368576",
                "6674325081096720384",
                "6674324192311115776",
                "6674323664088858624",
                "6674323546598014976",
                "6674323499869274112",
                "6674322320342908928",
                "6674320463285456896",
                "6674320442670448640",
                "6674320051790680064",
                "6674319463128502272",
                "6674318598707609600",
                "6674318481011245056",
                "6674318346684465152",
                "6674318087468093440",
                "6674316875213574144",
                "6674316771459072000",
                "6674316597114437632",
                "6674316285628649472",
                "6674316227134881792",
                "6674315186523865088",
                "6674314975881728000",
                "6674314930050568192",
                "6674314684373401600",
                "6674314011401519104",
                "6674313820141260800",
                "6674313663630807040",
                "6674313504754761728",
                "6674313227360272384",
                "6674313188076425216",
                "6674312235138940928",
                "6674311928111693824",
                "6674311746217316352",
                "6674311479866429440",
                "6674311003053756416",
                "6674310716339523584",
                "6674310704595468288",
                "6674310676908871680",
                "6674310613780402176",
                "6674310475045408768",
                "6674310333240184832",
                "6674309641767223296",
                "6674309463245062144",
                "6674309367858200576",
                "6674308909303336960",
                "6674308905759145984",
                "6674308719850819584",
                "6674308654029606912",
                "6674307714253852672",
                "6674307585648103424",
                "6674307281795944448",
                "6674307123007983616",
                "6674306723882205184",
                "6674306703804076032",
                "6674306635210424320",
                "6674305980873834496",
                "6674305570784149504",
                "6674305030813650944",
                "6674304749195493376",
                "6674304742648188928",
                "6674304713615216640",
                "6674304695483236352",
                "6674304556752441344",
                "6674304306868387840",
                "6674304133182263296",
                "6674304112063938560",
                "6674304027573882880",
                "6674302645299052544",
                "6674301944657346560",
                "6674301246146347008",
                "6674301150121951232",
                "6674301080139988992",
                "6674300851688833024",
                "6674300844344610816",
                "6674299704882237440",
                "6674299077380804608",
                "6674298935122595840",
                "6674298740179734528",
                "6674298473371664384",
                "6674297630224285696",
                "6674297296357687296",
                "6674295991736205312",
                "6674295687716274176",
                "6674290117802201088",
                "6674289235077369856",
                "6674288484536029184",
                "6674287951058309120",
                "6674287717318135808",
                "6674274831908737024",
                "6674258992010629120",
                "6674249013660745728",
                "6674245662206332928",
                "6674233350766465024",
                "6674226443779571712",
                "6674220779694981120",
                "6674220482159448064",
                "6674218489802133504",
                "6674212824371171328",
                "6674206757708894208",
                "6674205850631929856",
                "6674205091328688128",
                "6674203863194537984",
                "6674203180798050304",
                "6674201854684954624",
                "6674201581287641088",
                "6674200764883144704",
                "6674200696243359744",
                "6674200585966718976",
                "6674200539007291392",
                "6674200288611532800",
                "6674199994796343296",
                "6674199861589446656",
                "6674199602326933504",
                "6674193474285211648",
                "6674188992969183232",
                "6674187632446013440",
                "6674187010858549248",
                "6674175981642584064",
                "6674173666495823872",
                "6674169226405613568",
                "6674160382778544128",
                "6674158511674359808",
                "6674155713763217408",
                "6674154582454566912",
                "6674153028859199488",
                "6674133563627016192",
                "6674106290521772032",
                "6674077282241347584",
                "6674058610563092480",
                "6674057662264508416",
                "6674057006145339392",
                "6674023329222889472",
                "6674001741542854656",
                "6673996824203759616",
                "6673978949904633856",
                "6673956375166455808",
                "6673954874738081792",
                "6673947384721969152",
                "6673918412097323008",
                "6673909942455369728",
                "6673903074483896320",
                "6673885867045556224",
                "6673882720575160320",
                "6673881780065406976",
                "6673877383927304192",
                "6673877354567176192",
                "6673864394071343104",
                "6673864292309139456",
                "6673860754443079680",
                "6673858451577245696",
                "6673850738872942592",
                "6673844379037204480",
                "6673840854890708992",
                "6673837061914492928",
                "6673822093659869184",
                "6673820019706564608",
                "6673761316282302464",
                "6673760659538186240",
                "6673756221150855168",
                "6673755172876849152",
                "6673751276410372096",
                "6673747425452101632",
                "6673715925776076800",
                "6673621651969544192",
                "6673621556070977536",
                "6673609370414092288",
                "6673605478188056576",
                "6673603696179286016",
                "6673598344700366848",
                "6673596507834290176",
                "6673596050214752256",
                "6673594567410843648",
                "6673594334756995072",
                "6673593406809182208",
                "6673593003929505792",
                "6673592689415426048",
                "6673591971262500864",
                "6673590226209738752",
                "6673589877419802624",
                "6673588560114749440",
                "6673586679275589632",
                "6673585941388460032",
                "6673584893785542656",
                "6673584241810345984",
                "6673583205188108288",
                "6673582876518252544",
                "6673582344462405632",
                "6673580801734475776",
                "6673576917637005312",
                "6673575992730058752",
                "6673570399764090880",
                "6673569581631541248",
                "6673551396630953984",
                "6673551282050957312",
                "6673502736463433728",
                "6673496615837237248",
                "6673472291411591168",
                "6673471871326879744",
                "6673470998836154368",
                "6673455126079864832",
                "6673441478527680512",
                "6673422130845061120",
                "6673417698778550272",
                "6673416686722351104",
                "6673414190117425152",
                "6673408532366229504",
                "6673407238322782208",
                "6673395143120261120",
                "6673394684619915264",
                "6673393479852232704",
                "6673392687527235584",
                "6673390888401506304",
                "6673388994631303168",
                "6673386704499376128",
                "6673383335777013760",
                "6673374023721680896",
                "6673371064917037056",
                "6673364603000324096",
                "6673362250960474112",
                "6673314979451502592",
                "6673286645174763520",
                "6673274825273769984",
                "6673273139457167360",
                "6673272777220292608",
                "6673264826401165312",
                "6673259478013448192",
                "6673259172634562560",
                "6673258653128069120",
                "6673252491351556096",
                "6673251380796325888",
                "6673250151546490880",
                "6673248098468237312",
                "6673248074954964992",
                "6673246569367605248",
                "6673242164270469120",
                "6673241000250441728",
                "6673238955002957824",
                "6673233836475682816",
                "6673231648269537280",
                "6673228920147410944",
                "6673222971370246144",
                "6673221780670251008",
                "6673221578957783040",
                "6673217942886285312",
                "6673217128151126016",
                "6673214922194685952",
                "6673214911310462976",
                "6673214044926644224",
                "6673211599974563840",
                "6673210105061703680",
                "6673210085067456512",
                "6673208873760854016",
                "6673205924447916032",
                "6673205128805220352",
                "6673204698603851776",
                "6673204286698029056",
                "6673203587847294976",
                "6673202380516884480",
                "6673201851225083904",
                "6673200214976757760",
                "6673200122291027968",
                "6673199241952755712",
                "6673196947785912320",
                "6673193593336758272",
                "6673191229284417536",
                "6673189052713271296",
                "6673184335094349824",
                "6673183521567150080",
                "6673182193285595136",
                "6673181893623545856",
                "6673181490060201984",
                "6673180539140182016",
                "6673180364585832448",
                "6673178062424637440",
                "6673177760623493120",
                "6673175396239806464",
                "6673174762446917632",
                "6673174691655454720",
                "6673172818621894656",
                "6673172756760100864",
                "6673172752347693056",
                "6673170076050395136",
                "6673169960753168384",
                "6673169849981599744",
                "6673169825130352640",
                "6673169346300219392",
                "6673169193388474368",
                "6673167718704746496",
                "6673166126676967424",
                "6673166106930188288",
                "6673164978649829376",
                "6673164024806375424",
                "6673163929239162880",
                "6673162323684753408",
                "6673161625194729472",
                "6673160733225648128",
                "6673157202380853248",
                "6673153067052961792",
                "6673151773714153472",
                "6673149789409906688",
                "6673148665856524288",
                "6673148522092560384",
                "6673148284581711872",
                "6673147510824894464",
                "6673145465527074816",
                "6673145232848064512",
                "6673138115781070848",
                "6673137931797925888",
                "6673137618177232896",
                "6673117838883946496",
                "6673115061717827584",
                "6673114300049002496",
                "6673109919509516288",
                "6673097889754910720",
                "6673095670477357056",
                "6673087420616937472",
                "6673085487818735616",
                "6673084999698219008",
                "6673081900959010816",
                "6673078302195847168",
                "6673078095697678336",
                "6673076357393547264",
                "6673076092762324992",
                "6673068456557936640",
                "6673067954248089600",
                "6673064247473213440",
                "6673062648319315968",
                "6673061709319503872",
                "6673056707775561728",
                "6673054974022258688",
                "6673052219522154496",
                "6673049148649897984",
                "6673037981974003712",
                "6673031992168157184",
                "6672999611969310720",
                "6672889389049184256",
                "6672886055454113792",
                "6672879567331393536",
                "6672876324719169536",
                "6672875941309448192",
                "6672875483345977344",
                "6672874822579523584",
                "6672871290077974528",
                "6672870304617861120",
                "6672868771616526336",
                "6672867871355310080",
                "6672866946830045184",
                "6672865795120304128",
                "6672864758623895552",
                "6672860822219259904",
                "6672856184040456192",
                "6672854018047348736",
                "6672852994536505344",
                "6672847128001249280",
                "6672842784191283200",
                "6672830669975982080",
                "6672828061618012160",
                "6672799728230469632",
                "6672786303408406528",
                "6672786086902632448",
                "6672781481133211648",
                "6672773709805125632",
                "6672771571100160000",
                "6672771185601679360",
                "6672766938709495808",
                "6672763491436199936",
                "6672752075765452800",
                "6672746550914977792",
                "6672745298999115776",
                "6672742099680493568",
                "6672741198341345280",
                "6672740676196634624",
                "6672739298044805120",
                "6672738299154530304",
                "6672737965229211648",
                "6672737872249884672",
                "6672736284038922240",
                "6672736275679678464",
                "6672736223934545920",
                "6672725053798354944",
                "6672722953911668736",
                "6672719787551887360",
                "6672712923036520448",
                "6672699048689926144",
                "6672696092825485312",
                "6672693937896947712",
                "6672473337362382848",
                "6672432525907857408",
                "6672416084940820480",
                "6672400875111059456",
                "6672375747732045824",
                "6672327534102708224",
                "6672284930740850688",
                "6672142649157226496",
                "6671945763783708672",
                "6671856707578101760",
                "6671847991353016320",
                "6671823348604866560",
                "6671762108952088576",
                "6671753599283888128",
                "6671739571241226240",
                "6671733523533008896",
                "6671731787623825408",
                "6671726380721373184",
                "6671702906535673856",
                "6671663553604222976",
                "6671655160118251520",
                "6671651293557161984",
                "6671604533887500288",
                "6671423094659223552",
                "6671422656396398592",
                "6671422568467009536",
                "6671410400963067904",
                "6671337168025161728",
                "6671336958792302592",
                "6671240937219297280",
                "6671240169116401664",
                "6671236604763312128",
                "6671226847264505856",
                "6671070307278651392",
                "6671066170533085184",
                "6671061049518592000",
                "6671060463071002624",
                "6671055814117031936",
                "6671025005477761024",
                "6671023348450852864",
                "6671021184651366400",
                "6671018405077712896",
                "6670992507104395264",
                "6670987010129268736",
                "6669849145622134784");
    }

    private List<String> getUserIdListTest() {
        return Arrays.asList(
                "0",
                "1",
                "11",
                "12",
                "13",
                "14",
                "15",
                "16",
                "17",
                "18",
                "19",
                "2",
                "20",
                "21",
                "22",
                "23",
                "24",
                "25",
                "26",
                "27",
                "28",
                "29",
                "3",
                "30",
                "31",
                "32",
                "33",
                "34",
                "35",
                "36",
                "37",
                "38",
                "39",
                "4",
                "40",
                "41",
                "42",
                "43",
                "44",
                "45",
                "46",
                "47",
                "48",
                "49",
                "5",
                "50",
                "51",
                "52",
                "53",
                "54",
                "55",
                "56",
                "57",
                "58",
                "59",
                "6",
                "60",
                "61",
                "62",
                "63",
                "64",
                "65",
                "66",
                "6669577959050838016",
                "67",
                "68",
                "69",
                "7",
                "70",
                "71",
                "72",
                "73",
                "74",
                "75",
                "76",
                "77",
                "78",
                "79",
                "8",
                "80",
                "81",
                "82",
                "83",
                "84",
                "85",
                "86",
                "87",
                "88",
                "89",
                "9",
                "90",
                "91",
                "92",
                "93",
                "94");
    }
}
