package org.dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.kylin.dict.StringBytesConverter;
import org.apache.kylin.dict.TrieDictionary;
import org.apache.kylin.dict.TrieDictionaryBuilder;
import org.darts.DoubleArrayTrie;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

public class Benchmark {

    public static void printHelp() {
        System.out.println("Usage: DictionaryBenchmark -dict [DictPath]");
        System.out.println("e.g. : DictionaryBenchmark -dict /tmp/dict.big");
    }

    public static void main(String[] argv) throws IOException {
        // TODO Auto-generated method stub
        if (null == argv || 2 > argv.length) {
            printHelp();
        }
        String dictpath  = null;
        int    querytime = 10 * 1000 * 1000; //default lookup times is 10m
        for (int i = 0; i < argv.length; i++) {
            if ("-dict".equals(argv[i])) {
                if (i == argv.length - 1) {
                    printHelp();
                    return;
                }
                dictpath = argv[++i];
            } else if ("-times".equals(argv[i])) {
                if (i == argv.length - 1) {
                    printHelp();
                    return;
                }
                querytime = Integer.valueOf(argv[++i]);
            }
        }
        System.out.println("Benchmark about {HashMap,DAT,RadixTree,TrieDict} Structures for Dictionary");
        System.out.println("  HashMap: java.util.HashMap");
        System.out.println("  DAT (Double Array Trie): https://github.com/komiya-atsushi/darts-java");
        System.out.println("  RadixTree: https://github.com/npgall/concurrent-trees");
        System.out.println("  TrieDict (Dictionary in Kylin): http://kylin.apache.org/blog/2015/08/13/kylin-dictionary");
        System.out.println("================Test Result================");

        Runtime s_runtime = Runtime.getRuntime();
        long size1 = s_runtime.totalMemory() - s_runtime.freeMemory();
        List<String> wordList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(dictpath)));
        String line = null;
        while ((line = br.readLine()) != null) {
            wordList.add(line);

        }
        br.close();
        System.gc();
        long size2 = s_runtime.totalMemory() - s_runtime.freeMemory();
        byte[][] bWordList = new byte[wordList.size()][];
        for (int i = 0; i < wordList.size(); i++) {
            bWordList[i] = wordList.get(i).getBytes("UTF-8");
        }
        int n = wordList.size();
        int times = querytime / n == 0 ? 1 : querytime / n ;
        System.gc();
        long size3 = s_runtime.totalMemory() - s_runtime.freeMemory();
        System.out.println("a. Dictionary Size:" + wordList.size());
        System.out.println("--------");
        System.out.println();

        long start1 = System.currentTimeMillis();
        DoubleArrayTrie dat = new DoubleArrayTrie();
        dat.build(wordList);
        long end1 = System.currentTimeMillis();
        System.gc();
        long size4 = s_runtime.totalMemory() - s_runtime.freeMemory();

        long start2 = System.currentTimeMillis();
        HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
        for (int i = 0; i < wordList.size(); i++) {
            hashmap.put(wordList.get(i), i);
        }
        long end2 = System.currentTimeMillis();
        System.gc();
        long size5 = s_runtime.totalMemory() - s_runtime.freeMemory();

        long start3 = System.currentTimeMillis();
        NodeFactory nodeFactory = new DefaultCharArrayNodeFactory();
        ConcurrentRadixTree<Integer> tree = new ConcurrentRadixTree<Integer>(nodeFactory);
        for (int i = 0; i < n; i++) {
            tree.put(wordList.get(i), i);
        }
        long end3 = System.currentTimeMillis();
        System.gc();
        long size6 = s_runtime.totalMemory() - s_runtime.freeMemory();

        long start4 = System.currentTimeMillis();
        TrieDictionaryBuilder<String> b = new TrieDictionaryBuilder<String>(new StringBytesConverter());
        for (String s : wordList)
            b.addValue(s);
        TrieDictionary<String> tDict = b.build(0);
        long end4 = System.currentTimeMillis();
        System.gc();
        long size7 = s_runtime.totalMemory() - s_runtime.freeMemory();
        System.out.println("b. Build Time (ms) :");
        System.out.println("   DAT       : " + (end1 - start1));
        System.out.println("   HashMap   : " + (end2 - start2));
        System.out.println("   RadixTree : " + (end3 - start3));
        System.out.println("   TrieDict  : " + (end4 - start4));
        System.out.println("--------");
        System.out.println();

        System.out.println("c. Memory footprint in 64-bit JVM (byte) :");
        System.out.println("   DAT       : " + (size4 - size3));
        System.out.println("   HashMap   : " + (size5 - size4));
        System.out.println("   RadixTree : " + (size6 - size5));
        System.out.println("   TrieDict  : " + (size7 - size6));
        System.out.println("--------");
        System.out.println();

        long qstart1 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < n; j++) {
                if (dat.exactMatchSearch(wordList.get(j)) == -1) {
                    //throw new RuntimeException("没找到该有的词");
                }
            }
        }
        long qend1 = System.currentTimeMillis();

        long qstart2 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < n; j++) {
                hashmap.get(wordList.get(j));
            }
        }
        long qend2 = System.currentTimeMillis();

        long qstart3 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < n; j++) {
                tDict.getIdFromValueBytes(bWordList[j], 0, bWordList[j].length);
            }
        }
        long qend3 = System.currentTimeMillis();

        long qstart4 = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            for (int j = 0; j < n; j++) {
                tree.getValueForExactKey(wordList.get(j));
            }
        }
        long qend4 = System.currentTimeMillis();

        System.out.println("d. Retrieval Performance for " + (times * n) + " query times (ms) :");
        System.out.println("   DAT       : " + (qend1 - qstart1));
        System.out.println("   HashMap   : " + (qend2 - qstart2));
        System.out.println("   TrieDict  : " + (qend3 - qstart3));
        System.out.println("   RadixTree : " + (qend4 - qstart4));
        System.out.println("================Test Result================");
    }
}
