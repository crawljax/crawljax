package com.crawljax.core.state;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * The Path a Crawler has taken, or is about to backtrack on.
 */
public class CrawlPath extends ForwardingList<Eventable> {

    private final List<Eventable> eventablePath;

    private int backtrackTarget;
    private boolean backtrackSuccess;
    private int reachedNearDup;

    /**
     * Start a new empty CrawlPath.
     */
    public CrawlPath(int backtrackTarget) {
        this(Lists.newLinkedList(), backtrackTarget);
    }

    /**
     * Create a new CrawlPath based on a delegate.
     *
     * @param delegate the List implementation where this CrawlPath is based on.
     */
    public CrawlPath(List<Eventable> delegate, int backtrackTarget) {
        this.eventablePath = delegate;
        this.backtrackTarget = backtrackTarget;
        this.backtrackSuccess = false;
        this.reachedNearDup = -1;
    }

    public static CrawlPath copyOf(List<Eventable> eventable, int backtrackTarget) {
        return new CrawlPath(Lists.newLinkedList(eventable), backtrackTarget);
    }

    @Override
    protected List<Eventable> delegate() {
        return eventablePath;
    }

    /**
     * Get the last Eventable in the path.
     *
     * @return the last Eventable in the path
     */
    public Eventable last() {
        if (eventablePath.isEmpty()) {
            return null;
        }
        return eventablePath.get(eventablePath.size() - 1);
    }

    /**
     * Create an immutableCopy of the current CrawlPath, used for backtracking for giving them to
     * plugins.
     *
     * @return the CrawlPath based on an immutable list.
     */
    public CrawlPath immutableCopy() {
        return immutableCopy(false);
    }

    public CrawlPath immutableCopyWithoutLast() {
        return immutableCopy(true);
    }

    private CrawlPath immutableCopy(boolean removeLast) {
        if (isEmpty()) {
            return new CrawlPath(backtrackTarget);
        }

        // Build copy
        List<Eventable> path = Lists.newArrayList(this);

        // This is safe because checked above
        if (removeLast) {
            path.remove(path.size() - 1);
        }
        return new CrawlPath(ImmutableList.copyOf(path), this.backtrackTarget);
    }

    public int getBacktrackTarget() {
        return backtrackTarget;
    }

    public void setBacktrackTarget(int backtrackTarget) {
        this.backtrackTarget = backtrackTarget;
    }

    public boolean isBacktrackSuccess() {
        return backtrackSuccess;
    }

    public void setBacktrackSuccess(boolean backtrackSuccess) {
        this.backtrackSuccess = backtrackSuccess;
    }

    public int isReachedNearDup() {
        return reachedNearDup;
    }

    public void setReachedNearDup(int reachedNearDup) {
        this.reachedNearDup = reachedNearDup;
    }

    /**
     * Build a stack trace for this path. This can be used in generating more meaningful exceptions
     * while using Crawljax in conjunction with JUnit for example.
     *
     * @return a array of StackTraceElements denoting the steps taken by this path. The first element
     * [0] denotes the last {@link Eventable} on this path while the last item denotes the first
     * {@link Eventable} executed.
     */
    public StackTraceElement[] asStackTrace() {
        int i = 1;
        StackTraceElement[] list = new StackTraceElement[this.size()];
        for (Eventable e : this) {
            list[this.size() - i] = new StackTraceElement(
                    e.getEventType().toString(),
                    e.getIdentification().toString(),
                    e.getElement().toString(),
                    i);
            i++;
        }
        return list;
    }
}
