package tag.egypt.com.network;

import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.Call;

/**
 * Thread-safe cancellation token for active AI generation and network requests in TAJ EGY.
 */
public final class CancellationToken {
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private volatile Call activeCall;

    public void attachCall(Call call) {
        this.activeCall = call;
        if (isCancelled.get() && call != null) {
            call.cancel();
        }
    }

    public void cancel() {
        if (isCancelled.compareAndSet(false, true)) {
            Call call = activeCall;
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }
}
