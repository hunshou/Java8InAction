package lambdasinaction.practice;

import lambdasinaction.chap1.FilteringApples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * Created by hunshou on 2021/2/18.
 * https://www.cnblogs.com/zxf330301/p/6586750.html
 */
public class StreamDemo {

    public static void main(String[] args) {
//        createTest();
//        changeTest();
//        reductionTest();
//        collectTest();
//        groupingTest();
//        primitiveTest();
        parallelTest();
//        stringTest();
    }

    /**
     * primitive
     */
    public static void parallelTest() {
//        Stream支持并发操作，但需要满足以下几点：
//        构造一个paralle stream，默认构造的stream是顺序执行的，调用 paralle() 构造并行的stream：
        IntStream scoreStream = IntStream.rangeClosed(10, 30).parallel();

//        要执行的操作必须是可并行执行的，即并行执行的结果和顺序执行的结果是一致的，而且必须保证stream中执行的操作是线程安全的：
        int[] wordLength = new int[12];
        Stream.of("It", "is", "your", "responsibility").parallel().forEach(s -> {
            if (s.length() < 12) {
                wordLength[s.length()]++;
            }
        });
//        这段程序的问题在于，多线程访问共享数组 wordLength ，是非线程安全的。
//          解决的思路有：1）构造AtomicInteger数组；2）使用 groupingBy() 根据length统计；
//        可以通过并行提高效率的常见场景
//        使stream无序：对于 distinct() 和 limit() 等方法，如果不关心顺序，则可以使用并行：
        LongStream.rangeClosed(5, 10).unordered().parallel().limit(3);
        IntStream.of(14, 15, 15, 14, 12, 81).unordered().parallel().distinct();

//        在 groupingBy() 的操作中，map的合并操作是比较重的，可以通过 groupingByConcurrent() 来并行处理，不过前提是parallel stream：
//        Stream.of(getList()).parallel().collect(Collectors.groupingByConcurrent(City::getState));

//        在执行stream操作时不能修改stream对应的collection
//        stream本身是不存储数据的，数据保存在对应的collection中，所以在执行stream操作的同时修改对应的collection，结果是未定义的：
// ok
        List<String> wordList = Arrays.asList("regular", "expression", "specified", "as", "a", "string", "must");
        Stream<String> wordStream = wordList.stream();
//        wordList.add("number");
        wordStream.distinct().count();


// ConcurrentModificationException
//        Stream<String> wordStream = wordList.stream();
//        wordStream.forEach(s -> { if (s.length() >= 6) wordList.remove(s);});

        System.out.println("parallelTest");
    }

    /**
     * primitive
     */
    public static void primitiveTest() {
        //Stream<Integer> 对应的Primitive Stream就是 IntStream ，类似的还有 DoubleStream 和 LongStream 。

        //Primitive Stream的构造： of() , range() , rangeClosed() , Arrays.stream() :
        IntStream intStream = IntStream.of(10, 20, 30);
        IntStream zeroToNintyNine = IntStream.range(0, 100);
        IntStream zeroToHundred = IntStream.rangeClosed(0, 100);
        double[] nums = {10.0, 20.0, 30.0};
        DoubleStream doubleStream = Arrays.stream(nums, 0, 3);

        //Object Stream与Primitive Stream之间的相互转换，通过 mapToXXX() 和 boxed() ：
        // map to
        Stream<String> cityStream = Stream.of("Beijing", "Tianjin", "Chengdu");
        IntStream lengthStream = cityStream.mapToInt(String::length);

        // box
        Stream<Integer> oneToNine = IntStream.range(0, 10).boxed();

        //与Object Stream相比，Primitive Stream的特点：
        //toArray() 方法返回的是对应的Primitive类型：
        int[] intArr = intStream.toArray();
        intStream = IntStream.of(10, 20);
        //自带统计类型的方法，如： max() , average() , summaryStatistics() :
        OptionalInt maxNum = intStream.max();
        intStream = IntStream.of(10, 30);
        IntSummaryStatistics intSummary = intStream.summaryStatistics();

        System.out.println("primitiveTest");
    }

    public static List<City> getList() {

        City c1 = new City("c1", "c1s", "c1p");
        City c2 = new City("c2", "c2s", "c2p");
        City c3 = new City("c3", "c3s", "c3p");
        List<City> citys = new LinkedList<>();
        citys.add(c1);
        citys.add(c2);
        citys.add(c3);
        return citys;
    }

    /**
     * groupingByConcurrent() 是其并发版本：
     */
    public static void groupingTest() {

        //groupingBy() 表示根据某一个字段或条件进行分组，返回一个Map，其中key为分组的字段或条件，value默认为list，
        Map<String, List<Locale>> countryToLocaleList = Stream.of(Locale.getAvailableLocales())
                .collect(Collectors.groupingBy(l -> l.getDisplayCountry()));

        //如果 groupingBy() 分组的依据是一个bool条件，则key的值为true/false，此时与 partitioningBy() 等价，且 partitioningBy() 的效率更高：
        // predicate
        Map<Boolean, List<Locale>> englishAndOtherLocales = Stream.of(Locale.getAvailableLocales())
                .collect(Collectors.groupingBy(l -> l.getDisplayLanguage().equalsIgnoreCase("English")));

        // partitioningBy
        Map<Boolean, List<Locale>> englishAndOtherLocales2 = Stream.of(Locale.getAvailableLocales())
                .collect(Collectors.partitioningBy(l -> l.getDisplayLanguage().equalsIgnoreCase("English")));


        //roupingBy() 提供第二个参数，表示 downstream ，即对分组后的value作进一步的处理 返回set，而不是list：
        Map<String, Set<Locale>> countryToLocaleSet = Stream.of(Locale.getAvailableLocales())
                .collect(Collectors.groupingBy(l -> l.getDisplayCountry(), Collectors.toSet()));

        //返回value集合中元素的数量：
        Map<String, Long> countryToLocaleCounts = Stream.of(Locale.getAvailableLocales())
                .collect(Collectors.groupingBy(l -> l.getDisplayCountry(), Collectors.counting()));

        /*
        //对value集合中的元素求和：

        City c1 = new City("c1", "c1s", "c1p");
        City c2 = new City("c2", "c2s", "c2p");
        City c3 = new City("c3", "c3s", "c3p");
        List<City> citys = new LinkedList<>();
        citys.add(c1);
        citys.add(c2);
        citys.add(c3);
        Map<String, Integer> cityToPopulationSum = citys.stream()
                .collect(Collectors.groupingBy((City city) -> city.getName(), Collectors.summingInt(City::getPopulation)));

        //对value的某一个字段求最大值，注意value是Optional的：
        Map<String, Optional<City>> cityToPopulationMax = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getName, Collectors.maxBy(Comparator.comparing(City::getPopulation))));
        //使用mapping对value的字段进行map处理：
        Map<String, Optional<String>> stateToNameMax = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getState, Collectors.mapping(City::getName, Collectors.maxBy
                        (Comparator.comparing(String::length)))));

        Map<String, Set<String>> stateToNameSet = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getState, Collectors.mapping(City::getName, Collectors.toSet())));
        //通过 summarizingXXX 获取统计结果：
        Map<String, IntSummaryStatistics> stateToPopulationSummary = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getState, Collectors.summarizingInt(City::getPopulation)));
        //reducing() 可以对结果作更复杂的处理，但是 reducing() 却并不常用：
        Map<String, String> stateToNameJoining = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getState, Collectors.reducing("", City::getName,
                        (s, t) -> s.length() == 0 ? t : s + ", " + t)));
        //比如上例可以通过mapping达到同样的效果：

        Map<String, String> stateToNameJoining2 = Stream.of(cities)
                .collect(Collectors.groupingBy(City::getState, Collectors.mapping(City::getName, Collectors.joining(", ")
                )));

        */

        System.out.println("groupingTest");
    }

    /**
     * collect() 可以对stream中的元素进行各种处理后，得到stream中元素的值；
     */
    public static void collectTest() {
        //Collectors 接口提供了很方便的创建 Collector 对象的工厂方法：
// collect to Collection
        List<String> list = Stream.of("You", "may", "assume").collect(Collectors.toList());
        Set<String> set1 = Stream.of("You", "may", "assume").collect(Collectors.toSet());
        TreeSet<String> treeSet = Stream.of("You", "may", "assume").collect(Collectors.toCollection(TreeSet::new));

// join element
        String join = Stream.of("You", "may", "assume").collect(Collectors.joining());
        String joining = Stream.of("You", "may", "assume").collect(Collectors.joining(", "));

// summarize element
        //IntSummaryStatistics{count=3, sum=12, min=3, average=4.000000, max=6}
        IntSummaryStatistics summary = Stream.of("You", "may", "assume").collect(Collectors.summarizingInt(String::length));
        summary.getMax();

        //foreach() 方法 foreach() 用于遍历stream中的元素，属于 terminal operation ；
        Stream.of("You", "may", "assume", "you", "can", "fly")
                .parallel()
                .forEach(w -> System.out.println(w));
        //forEachOrdered() 是按照stream中元素的顺序遍历，也就无法利用并发的优势；
        Stream.of("You", "may", "assume", "you", "can", "fly")
                .forEachOrdered(w -> System.out.println(w));


        //toArray() 方法得到由stream中的元素得到的数组，默认是Object[]，可以通过参数设置需要结果的类型：
        Object[] words1 = Stream.of("You", "may", "assume").toArray();
        String[] words2 = Stream.of("You", "may", "assume").toArray(String[]::new);

        //toMap() 方法,将stream中的元素映射为 的形式，两个参数分别用于生成对应的key和value的值。
        //比如有一个字符串stream，将首字母作为key，字符串值作为value，得到一个map：
        Stream<String> introStream = Stream.of("Get started with UICollectionView and the photo library".split(" "));
        Map<String, String> introMap = introStream.collect(Collectors.toMap(s -> s.substring(0, 1), s -> s));

        //如果一个key对应多个value，则会抛出异常，需要使用第三个参数设置如何处理冲突，比如仅使用原来的value、使用新的value，或者合并：
        Stream<String> introStream1 = Stream.of("Get started with UICollectionView and the photo library".split(" "));
        Map<Integer, String> introMap2 = introStream1.collect(
                Collectors.toMap(s -> s.length(), s -> s,
                        (existingValue, newValue) -> existingValue)
        );

        //如果value是一个集合，即将key对应的所有value放到一个集合中，则需要使用第三个参数，将多个value合并：
        Stream<String> introStream3 = Stream.of("Get started with UICollectionView and the photo library".split(" "));
        Map<Integer, Set<String>> introMap3 = introStream3.collect(
                Collectors.toMap(
                        s -> s.length(),
                        s -> Collections.singleton(s),
                        (existingValue, newValue) -> {
                            HashSet<String> set = new HashSet<>(existingValue);
                            set.addAll(newValue);
                            return set;
                        }));
        introMap3.forEach((k, v) -> System.out.println(k + ": " + v));


        //如果value是对象自身，则使用 Function.identity() ，如：

        List<FilteringApples.Apple> inventory = Arrays.asList(new FilteringApples.Apple(80, "green"),
                new FilteringApples.Apple(155, "green"),
                new FilteringApples.Apple(120, "red"));

        Map<Integer, FilteringApples.Apple> idToEnt = inventory.stream()
                .collect(Collectors.toMap(
                        (FilteringApples.Apple a) -> a.getWeight(), Function.identity())
                );

        //toMap() 默认返回的是HashMap，如果需要其它类型的map，比如TreeMap，则可以在第四个参数指定构造方法：
        introStream = Stream.of("Get started with UICollectionView and the photo library".split(" "));
        Map<Integer, String> introMap4 = introStream
                .collect(Collectors.toMap(
                        s -> s.length(),
                        s -> s,
                        (existingValue, newValue) -> existingValue, TreeMap::new
                ));


        System.out.println("collectTest");
    }

    /**
     * 3. Stream reduction
     * reduce() 的结果是一个值；
     * reduction 就是从stream中取出结果，是 terminal operation ，因此经过 reduction 后的stream不能再使用了
     */
    public static void reductionTest() {
        //Optional 表示或者有一个T类型的对象，或者没有值；
        //创建Optional对象,直接通过Optional的类方法： of() / empty() / ofNullable() ：
        Optional<Integer> intOpt = Optional.of(10);
        Optional<String> emptyOpt = Optional.empty();
        Optional<Double> doubleOpt = Optional.ofNullable(5.5);

        //使用Optional对象
        Double d1 = doubleOpt.orElse(0.0);
        Double d2 = doubleOpt.orElseGet(() -> 1.0);
        doubleOpt.orElseThrow(RuntimeException::new);

        doubleOpt = Optional.ofNullable(4.5);
        List<Double> doubleList = new ArrayList<>();
        doubleOpt.ifPresent(doubleList::add);

        //map() 方法与 ifPresent() 用法相同，就是多了个返回值， flatMap() 用于Optional的链式表达：
        Optional<Boolean> addOk = doubleOpt.map(doubleList::add);
        Optional o1 = Optional.of(4.0)
                .flatMap(num -> Optional.ofNullable(num * 100))
                .flatMap(num -> Optional.ofNullable(Math.sqrt(num)));

        //4.2 简单的reduction 主要包含以下操作： findFirst() / findAny() / allMatch / anyMatch() / noneMatch ，比如：
        Stream<String> wordStream = Stream.of("Beijing", "Shanghai", "Chengdu", "Yw", "Yiyang", "HZ");
        Stream<String> wordStream1 = Stream.of("Yw", "Yiyang", "HZ");
        Stream<String> wordStream2 = Stream.of("Yw", "Yiyang", "HZ");
        Stream<String> wordStream3 = Stream.of("Yw", "Yiyang", "HZ");
        Stream<String> wordStream4 = Stream.of("Yw", "Yiyang", "HZ");
        Optional<String> firstWord = wordStream.filter(s -> s.startsWith("Y")).findFirst();
        Optional<String> anyWord = wordStream1.filter(s -> s.length() > 3).findAny();
        boolean match1 = wordStream2.allMatch(s -> s.length() > 3);
        boolean match2 = wordStream3.anyMatch(s -> s.length() > 3);
        boolean match3 = wordStream4.noneMatch(s -> s.length() > 3);


        //reduce(accumulator) ：参数是一个执行双目运算的 Functional Interface ，
        // 假如这个参数表示的操作为op，stream中的元素为x, y, z, …，
        // 则 reduce() 执行的就是 x op y op z ...，
        // 所以要求op这个操作具有结合性(associative)，即满足： (x op y) op z = x op (y op z)，
        // 满足这个要求的操作主要有：求和、求积、求最大值、求最小值、字符串连接、集合并集和交集等。另外，该函数的返回值是Optional的：
        Stream<Integer> numStream = Stream.of(1, 2, 3);
        Optional<Integer> sum1 = numStream.reduce((x, y) -> x + y);

        //reduce(identity, accumulator) ：可以认为第一个参数为默认值，但需要满足 identity op x = x ，
        // 所以对于求和操作， identity 的值为0，对于求积操作， identity 的值为1。返回值类型是stream元素的类型：
        numStream = Stream.of(1, 2, 3, 4);
        Integer sum2 = numStream.reduce(0, Integer::sum);

        System.out.println("reductionTest");
    }

    /**
     * 3. Stream转换
     */
    public static void changeTest() {
        //filter() 用于过滤，即使原stream中满足条件的元素构成新的stream
        List<String> langList = Arrays.asList("Java", "Python", "Swift", "HTML");
        Stream<String> filterStream = langList.stream().
                filter(lang -> lang.equalsIgnoreCase("java"));
        printStream(filterStream);

        //map() 用于映射，遍历原stream中的元素，转换后构成新的stream：
        Stream<String> mapStream = langList.stream().map(String::toLowerCase);
        printStream(mapStream);

        //limit() 表示限制stream中元素的数量，
        Stream<Integer> limitStream = Stream.of(18, 20, 12, 35, 89)
                .sorted()
                .limit(3);
        printStream(limitStream);

        //skip() 表示跳过stream中前几个元素，
        Stream<Integer> skipStream = Stream.of(18, 20, 12, 35, 89)
                .sorted(Comparator.reverseOrder())
                .skip(1);
        printStream(skipStream);

        // concat 表示将多个stream连接起来，
        // peek() 主要用于debug时查看stream中元素的值：
        // peek() 是 intermediate operation ，所以后面需要一个 terminal operation ，如 count() 才能在输出中看到结果；
        Stream<Integer> concatStream = Stream.concat(Stream.of(1, 2, 3), Stream.of(4, 5, 6));
        concatStream.peek(i -> System.out.println(i)).count();

        //有状态的(stateful)转换，即元素之间有依赖关系，如 distinct() 返回由唯一元素构成的stream，
        Stream<String> distinctStream = Stream.of("Beijing", "Tianjin", "Beijing").distinct();
        printStream(distinctStream);

        //sorted() 返回排序后的stream：
        Stream<String> sortedStream = Stream.of("Beijing", "Shanghai", "Chengdu")
                .sorted(Comparator.comparing(String::length));
        printStream(sortedStream);

        //逆序
        Stream<String> reRortedStream = Stream.of("Beijing", "Shanghai", "Chengdu")
                .sorted(Comparator.comparing(String::length).reversed());
        printStream(reRortedStream);

    }

    public static void createTest() {

        //从array或list创建stream：
        Stream<Integer> integerStream = Stream.of(10, 20, 30, 40);
        String[] cityArr = {"Beijing", "Shanghai", "Chengdu"};

        Stream<String> cityStream = Stream.of(cityArr);
        Stream<String> cityStream2 = Arrays.stream(cityArr, 0, 1);//Beijing
        printStream(cityStream2);

        Stream<String> nameStream = Arrays.asList("Daniel", "Peter", "Kevin").stream();
        Stream<String> emptyStream = Stream.empty();

        //通过 generate 和 iterate 创建无穷stream：
        Stream<String> echos = Stream.generate(() -> "echo");
        Stream<Integer> integers = Stream.iterate(0, num -> num + 1);

        //通过其它API创建stream：
        try {
            Stream<String> lines = Files.lines(Paths.get("test.txt"));

            String content = "AXDBDGXC";
            Stream<String> contentStream = Pattern.compile("[ABC]{1,3}").splitAsStream(content);

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("createTest");

    }

    public static void stringTest() {
        List<String> wordList = Arrays.asList("regular", "expression", "specified", "as", "a", "string", "must");

        //统计其中长度大于7的字符串的数量
        long countByStream = wordList.stream()
                .filter(w -> w.length() > 7)
                .count();
        //并发
        long countByParallelStream = wordList.parallelStream().filter(w -> w.length() > 7).count();


        System.out.println("stringTest");
    }


    public static <T> void printStream(Stream<T> stream) {
        System.out.println(stream.collect(Collectors.toList()));
    }

}
