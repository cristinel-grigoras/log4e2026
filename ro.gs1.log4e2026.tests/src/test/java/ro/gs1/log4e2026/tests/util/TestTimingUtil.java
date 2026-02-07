package ro.gs1.log4e2026.tests.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
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
     * Returns quickly - SWTBot handles polling.
     */
    public static ICondition shellCountIncreases(SWTWorkbenchBot bot, int initialCount) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
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
     * Returns quickly - SWTBot handles polling.
     */
    public static ICondition activeShellChanges(SWTWorkbenchBot bot, String initialShellText) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
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
     * Returns quickly - SWTBot handles polling.
     */
    public static ICondition editorIsActive(SWTWorkbenchBot bot, String editorTitle) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
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
     * Condition: wait for editor styled text content to change from original.
     * Use after handler actions that modify the editor.
     */
    public static ICondition editorContentChanges(SWTBotStyledText styledText, String originalContent) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
                return !styledText.getText().equals(originalContent);
            }
            @Override
            public void init(SWTBot bot) {}
            @Override
            public String getFailureMessage() {
                return "Editor content did not change";
            }
        };
    }

    /**
     * Compact debug log - single line with view, perspective, shell info.
     * Prints a dot at the end when method finishes.
     */
    public static void log(SWTWorkbenchBot bot, String step) {
        // Print step immediately without bot queries
        System.out.print("[" + step + "] ");
        System.out.flush();

        StringBuilder sb = new StringBuilder();

        // Active perspective
        try {
            String persp = bot.activePerspective().getLabel();
            sb.append("persp='").append(persp).append("' ");
        } catch (Exception e) {
            sb.append("persp=ERR ");
        }

        // Active view
        try {
            String view = bot.activeView().getTitle();
            sb.append("view='").append(view).append("' ");
        } catch (Exception e) {
            // No active view is common, don't log error
        }

        // Active shell
        try {
            sb.append("shell='").append(bot.activeShell().getText()).append("' ");
        } catch (Exception e) {
            sb.append("shell=ERR ");
        }

        // Check for open shells (dialogs, menus) - show shell count and names
        try {
            org.eclipse.swtbot.swt.finder.widgets.SWTBotShell[] shells = bot.shells();
            if (shells.length > 1) {
                StringBuilder shellsSb = new StringBuilder();
                shellsSb.append("shells(").append(shells.length).append(")=[");
                int count = 0;
                for (org.eclipse.swtbot.swt.finder.widgets.SWTBotShell shell : shells) {
                    try {
                        String text = shell.getText();
                        if (text != null && !text.isEmpty()) {
                            shellsSb.append(text).append(",");
                            if (++count >= 5) { shellsSb.append("..."); break; }
                        }
                    } catch (Exception e) {}
                }
                if (shellsSb.charAt(shellsSb.length()-1) == ',') {
                    shellsSb.setLength(shellsSb.length()-1);
                }
                shellsSb.append("] ");
                sb.append(shellsSb);
            }
        } catch (Exception e) {
            // Error getting shells
        }

        // Show focused widget info
        try {
            org.eclipse.swt.widgets.Widget focused = bot.getFocusedWidget();
            if (focused != null) {
                sb.append("focus=").append(focused.getClass().getSimpleName()).append(" ");
            }
        } catch (Exception e) {
            // No focused widget
        }

        // Buttons (if in dialog)
        try {
            StringBuilder btns = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                try { btns.append(bot.button(i).getText()).append(","); } catch (Exception e) { break; }
            }
            if (btns.length() > 0) sb.append("btns=[").append(btns.substring(0, btns.length()-1)).append("] ");
        } catch (Exception e) {}

        // Tree items (if present)
        try {
            StringBuilder tree = new StringBuilder();
            int count = 0;
            for (SWTBotTreeItem item : bot.tree().getAllItems()) {
                tree.append(item.getText()).append(",");
                if (++count >= 5) { tree.append("..."); break; }
            }
            if (tree.length() > 0) sb.append("tree=[").append(tree.substring(0, tree.length()-1)).append("]");
        } catch (Exception e) {}

        System.out.print(sb.toString().trim());
        System.out.println(".");  // Dot at end to show method finished
        System.out.flush();
    }

    /**
     * Clear all timing data.
     */
    public static void clear() {
        timings.clear();
    }

    /**
     * Condition: wait for a project with specific name to exist in the tree.
     * More robust than treeHasRows(1) when other projects may exist.
     */
    public static ICondition projectExists(SWTWorkbenchBot bot, String projectName) {
        return new ICondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    for (SWTBotTreeItem item : bot.tree().getAllItems()) {
                        if (item.getText().equals(projectName)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Tree not ready
                }
                return false;
            }
            @Override
            public void init(SWTBot bot) {}
            @Override
            public String getFailureMessage() {
                return "Project '" + projectName + "' not found in tree";
            }
        };
    }

    /**
     * Delete a project from the workspace if it exists. Use before creating
     * a project to handle retries from failed previous runs.
     * @return true if project was found and deleted
     */
    public static boolean deleteProjectIfExists(SWTWorkbenchBot bot, String projectName) {
        try {
            SWTBotTreeItem project = bot.tree().getTreeItem(projectName);
            System.out.println("  [deleteProjectIfExists] '" + projectName + "' exists, deleting...");
            System.out.flush();

            bot.closeAllEditors();
            focusWorkbenchShell(bot);
            project.select();
            project.contextMenu("Delete").click();

            waitUntil(bot, org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Delete Resources"), 3000);
            bot.checkBox("Delete project contents on disk (cannot be undone)").click();
            org.eclipse.swtbot.swt.finder.widgets.SWTBotShell deleteShell = bot.shell("Delete Resources");
            bot.button("OK").click();
            waitUntil(bot, org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses(deleteShell), 3000);

            System.out.println("  [deleteProjectIfExists] '" + projectName + "' deleted successfully");
            System.out.flush();
            return true;
        } catch (Exception e) {
            // Project does not exist or deletion failed - OK to proceed
            return false;
        }
    }

    /**
     * Focus the workbench shell (not internal Eclipse shells like "PartRenderingEngine's limbo").
     * This should be called in setUp() instead of bot.shells()[0].setFocus().
     */
    public static void focusWorkbenchShell(SWTWorkbenchBot bot) {
        for (org.eclipse.swtbot.swt.finder.widgets.SWTBotShell shell : bot.shells()) {
            try {
                String text = shell.getText();
                if (text != null && !text.isEmpty() && !text.contains("limbo")) {
                    shell.setFocus();
                    return;
                }
            } catch (Exception e) {
                // Continue to next shell
            }
        }
        // Fallback: try shells()[0] if nothing else works
        try {
            bot.shells()[0].setFocus();
        } catch (Exception e) {
            // Ignore
        }
    }
}
