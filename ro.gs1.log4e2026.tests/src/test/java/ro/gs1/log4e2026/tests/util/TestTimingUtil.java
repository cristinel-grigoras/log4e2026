package ro.gs1.log4e2026.tests.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.SWTBot;

/**
 * Utility to track execution time vs waiting time in UI tests.
 */
public class TestTimingUtil {

    private static final ConcurrentHashMap<String, TimingData> timings = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> currentTest = new ThreadLocal<>();

    public static class TimingData {
        public final String testName;
        public final long startTime;
        public final AtomicLong waitTime = new AtomicLong(0);
        public final AtomicLong sleepTime = new AtomicLong(0);
        public final AtomicLong waitUntilTime = new AtomicLong(0);
        public volatile long endTime;

        public TimingData(String testName) {
            this.testName = testName;
            this.startTime = System.currentTimeMillis();
        }

        public long getTotalTime() {
            return (endTime > 0 ? endTime : System.currentTimeMillis()) - startTime;
        }

        public long getTotalWaitTime() {
            return sleepTime.get() + waitUntilTime.get();
        }

        public long getExecutionTime() {
            return getTotalTime() - getTotalWaitTime();
        }

        @Override
        public String toString() {
            return String.format(
                "%-50s | Total: %6dms | Exec: %6dms | Wait: %6dms (sleep: %5dms, waitUntil: %5dms)",
                testName,
                getTotalTime(),
                getExecutionTime(),
                getTotalWaitTime(),
                sleepTime.get(),
                waitUntilTime.get()
            );
        }
    }

    /**
     * Start timing a test method.
     */
    public static void startTest(String testName) {
        currentTest.set(testName);
        timings.put(testName, new TimingData(testName));
    }

    /**
     * End timing a test method.
     */
    public static void endTest(String testName) {
        TimingData data = timings.get(testName);
        if (data != null) {
            data.endTime = System.currentTimeMillis();
        }
        currentTest.remove();
    }

    /**
     * Tracked sleep - use instead of Thread.sleep().
     */
    public static void sleep(long millis) throws InterruptedException {
        String test = currentTest.get();
        long start = System.currentTimeMillis();
        Thread.sleep(millis);
        long elapsed = System.currentTimeMillis() - start;
        if (test != null) {
            TimingData data = timings.get(test);
            if (data != null) {
                data.sleepTime.addAndGet(elapsed);
            }
        }
    }

    /**
     * Tracked waitUntil - wraps bot.waitUntil().
     * @return elapsed wait time in milliseconds
     */
    public static long waitUntil(SWTBot bot, ICondition condition) {
        String test = currentTest.get();
        long start = System.currentTimeMillis();
        bot.waitUntil(condition);
        long elapsed = System.currentTimeMillis() - start;
        if (test != null) {
            TimingData data = timings.get(test);
            if (data != null) {
                data.waitUntilTime.addAndGet(elapsed);
            }
        }
        return elapsed;
    }

    /**
     * Tracked waitUntil with timeout.
     * @return elapsed wait time in milliseconds
     */
    public static long waitUntil(SWTBot bot, ICondition condition, long timeout) {
        System.out.print("  >waitUntil(" + timeout + "ms)...");
        System.out.flush();
        String test = currentTest.get();
        long start = System.currentTimeMillis();
        bot.waitUntil(condition, timeout);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(" done in " + elapsed + "ms");
        System.out.flush();
        if (test != null) {
            TimingData data = timings.get(test);
            if (data != null) {
                data.waitUntilTime.addAndGet(elapsed);
            }
        }
        return elapsed;
    }

    /**
     * Tracked waitUntil with timeout and poll interval.
     * @return elapsed wait time in milliseconds
     */
    public static long waitUntil(SWTBot bot, ICondition condition, long timeout, long interval) {
        String test = currentTest.get();
        long start = System.currentTimeMillis();
        bot.waitUntil(condition, timeout, interval);
        long elapsed = System.currentTimeMillis() - start;
        if (test != null) {
            TimingData data = timings.get(test);
            if (data != null) {
                data.waitUntilTime.addAndGet(elapsed);
            }
        }
        return elapsed;
    }

    /**
     * Print summary of all test timings.
     */
    public static void printSummary() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("TEST TIMING SUMMARY");
        System.out.println("=".repeat(120));

        long totalExec = 0;
        long totalWait = 0;
        long totalTime = 0;

        for (TimingData data : timings.values()) {
            System.out.println(data);
            totalExec += data.getExecutionTime();
            totalWait += data.getTotalWaitTime();
            totalTime += data.getTotalTime();
        }

        System.out.println("-".repeat(120));
        System.out.println(String.format(
            "%-50s | Total: %6dms | Exec: %6dms | Wait: %6dms",
            "TOTAL (" + timings.size() + " tests)",
            totalTime,
            totalExec,
            totalWait
        ));
        System.out.println("=".repeat(120));
    }

    private static final int CONDITION_POLL_INTERVAL = 50; // ms

    /**
     * Condition: wait for shell count to increase (e.g., menu opened).
     */
    public static ICondition shellCountIncreases(SWTWorkbenchBot bot, int initialCount) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
                for (int i = 0; i < 10; i++) {
                    if (bot.shells().length > initialCount) {
                        return true;
                    }
                    Thread.sleep(CONDITION_POLL_INTERVAL);
                }
                return bot.shells().length > initialCount;
            }
            @Override
            public void init(SWTBot bot) {}
            @Override
            public String getFailureMessage() {
                return "Shell count did not increase from " + initialCount;
            }
        };
    }

    /**
     * Condition: wait for active shell to change.
     */
    public static ICondition activeShellChanges(SWTWorkbenchBot bot, String initialShellText) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
                for (int i = 0; i < 10; i++) {
                    String current = bot.activeShell().getText();
                    if (!current.equals(initialShellText)) {
                        return true;
                    }
                    Thread.sleep(CONDITION_POLL_INTERVAL);
                }
                return !bot.activeShell().getText().equals(initialShellText);
            }
            @Override
            public void init(SWTBot bot) {}
            @Override
            public String getFailureMessage() {
                return "Active shell did not change from '" + initialShellText + "'";
            }
        };
    }

    /**
     * Condition: wait for editor to be active.
     */
    public static ICondition editorIsActive(SWTWorkbenchBot bot, String editorTitle) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
                for (int i = 0; i < 10; i++) {
                    try {
                        bot.editorByTitle(editorTitle);
                        return true;
                    } catch (Exception e) {
                        Thread.sleep(CONDITION_POLL_INTERVAL);
                    }
                }
                try {
                    bot.editorByTitle(editorTitle);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            public void init(SWTBot bot) {}
            @Override
            public String getFailureMessage() {
                return "Editor '" + editorTitle + "' not found";
            }
        };
    }

    /**
     * Compact debug log - single line, only positive finds.
     */
    public static void log(SWTWorkbenchBot bot, String step) {
        // Print step immediately without bot queries
        System.out.print("[" + step + "] ");
        System.out.flush();

        StringBuilder sb = new StringBuilder();
        try { sb.append("shell='").append(bot.activeShell().getText()).append("' "); } catch (Exception e) { sb.append("shell=ERR "); }
        try {
            StringBuilder btns = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                try { btns.append(bot.button(i).getText()).append(","); } catch (Exception e) { break; }
            }
            if (btns.length() > 0) sb.append("btns=[").append(btns.substring(0, btns.length()-1)).append("] ");
        } catch (Exception e) {}
        try {
            StringBuilder tree = new StringBuilder();
            for (SWTBotTreeItem item : bot.tree().getAllItems()) tree.append(item.getText()).append(",");
            if (tree.length() > 0) sb.append("tree=[").append(tree.substring(0, tree.length()-1)).append("]");
        } catch (Exception e) {}
        System.out.println(sb.toString().trim());
        System.out.flush();
    }

    /**
     * Clear all timing data.
     */
    public static void clear() {
        timings.clear();
    }
}
