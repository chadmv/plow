package com.breakersoft.plow;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class FrameSet implements Iterable<Integer> {

    private static final List<Pattern> PATTERNS =
            ImmutableList.of(
                    Pattern.compile("^(-?[0-9]+)-(-?[0-9]+)$"),
                    Pattern.compile("^(-?[0-9]+)$"),
                    Pattern.compile("^(-?[0-9]+)-(-?[0-9]+)([xy:]{1})([0-9]+)$"));

    private final Set<Integer> set;
    private final List<Integer> list;
    private final String range;

    public FrameSet(String range) {
        this.range = range;
        this.set = Sets.newHashSet();
        this.list = Lists.newArrayList();
        parseFrameRange();
    }

    @Override
    public Iterator<Integer> iterator() {
        return list.iterator();
    }

    public int indexOf(int frame) {
        return list.indexOf(frame);
    }

    public int size() {
        return list.size();
    }

    public int get(int index) {
        return list.get(index);
    }

    public int last() {
        return list.get(list.size() - 1);
    }

    public int first() {
        return list.get(0);
    }

    public String toString() {
        return range;
    }

    public boolean contains(int frame) {
        return set.contains(frame);
    }

    private void parseFrameRange() {

        for (String part: range.split(",")) {
            for (int idx = 0; idx<PATTERNS.size(); idx++) {
                Matcher matcher = PATTERNS.get(idx).matcher(part);
                if (!matcher.matches()) {
                    continue;
                }

                int start;
                int end;
                int chunk;

                switch (idx) {
                case 0:
                    start = Integer.parseInt(matcher.group(1));
                    end = Integer.parseInt(matcher.group(2));
                    addFrames(range(start, end+1, 1));
                    break;
                case 1:
                    start = Integer.parseInt(matcher.group(1));
                    addFrame(start);
                    break;
                case 2:
                    start = Integer.parseInt(matcher.group(1));
                    end = Integer.parseInt(matcher.group(2));
                    chunk = Integer.parseInt(matcher.group(4));
                    String mod = matcher.group(3);

                    if (mod.equals("x")) {
                        addFrames(range(start, end+1, chunk));
                    }
                    else if(mod.equals("y")) {
                        final Set<Integer> bad = ImmutableSet.copyOf(
                                range(start, end+1, chunk));
                        for (int i: range(start, end+1, 1)) {
                            if (!bad.contains(i)) {
                                addFrame(i);
                            }
                        }
                    }
                    else if (mod.equals(":")) {
                        for (int stagger: range(chunk, 0, -1)) {
                            addFrames(range(start, end+1, stagger));
                        }
                    }
                    break;
                }
            }
        }
        if (list.size() == 0) {
            throw new IllegalArgumentException("Invalid/unparsable frame range:" + range);
        }
    }

    private void addFrame(int i) {
        if (!set.contains(i)) {
            list.add(i);
            set.add(i);
        }
    }

    private void addFrames(List<Integer> frames) {
        for (int i: frames) {
            if (!set.contains(i)) {
                list.add(i);
                set.add(i);
            }
        }
    }

    private static final List<Integer> range(int start, int end, int chunk) {

        if (chunk == 0) {
            throw new IllegalArgumentException("Chunk size cannot be 0.");
        }

        final int alloc = Math.abs((end - start) / chunk);
        final List<Integer> result =
                Lists.newArrayListWithExpectedSize(alloc);

        if (chunk < 0) {
            for (int i=start; i>end; i=i+chunk) {
                result.add(i);
            }
        } else if (chunk > 0) {
            for (int i=start; i<end; i=i+chunk) {
                result.add(i);
            }
        }
        return result;
    }
}
