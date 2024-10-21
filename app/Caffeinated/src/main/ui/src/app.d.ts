import { type Writable } from 'svelte/store';

export declare type Saucer = {
    /**
     * How often saucer should check for mutations. Default is 150ms.
     *
     * It is recommended you keep this value above 100ms.
     *
     * Note that you can change this on the fly safely.
     */
    MUTATION_POLL_RATE: number;

    /**
     * Close the Saucer window, exiting the run() loop.
     */
    close(): void;

    messages: SaucerMessages;
    webview: SaucerWebview;
    window: SaucerWindow;
};

/**
 * This is an opaque type.
 */
export declare type SaucerHandlerRegistrationId = {};

export declare type SaucerMessages = {
    /**
     * Registers a listener for receiving messages from the main process.
     */
    onMessage(callback: (data: any) => void): SaucerHandlerRegistrationId;

    /**
     * Unregisters a listener.
     */
    off(registrationId: SaucerHandlerRegistrationId): void;

    /**
     * Sends a message to the main process.
     */
    emit(data: any): void;
};

export declare type SaucerWebview = {
    /**
     * Whether or not the context menu is enabled (i.e the right-click menu).
     *
     * Set this value to change it.
     */
    contextMenuAllowed: Promise<boolean> | boolean;

    /**
     * Whether or not the dev tools window is visible.
     *
     * Set this value to change it.
     */
    devtoolsVisible: Promise<boolean> | boolean;

    /**
     * Returns the current URL.
     *
     * Set this value to change it.
     */
    url: Promise<string> | string;

    /**
     * The background color of the Saucer window. Note that this is
     *         different from the HTML background.
     *
     * Note that applying blur effects in HTML will not cause the apps behind the
     *           window to appear blurred.
     *
     * Set this value to change it.
     */
    background: Promise<SaucerColor> | SaucerColor;

    /**
     * Whether or not the `prefers-color-scheme` media query is forcibly set to `dark`
     *
     * Set this value to change it.
     */
    forceDarkAppearance: Promise<boolean> | boolean;

    /**
     * Navigates backward.
     */
    back(): void;

    /**
     * Navigates forward.
     */
    forward(): void;

    /**
     * Reloads the current page.
     */
    reload(): void;
};

export declare type SaucerSize = {
    width: number;
    height: number;
};

export declare type SaucerColor = {
    red: number;
    green: number;
    blue: number;
    alpha: number;
};

export declare type SaucerWindow = {
    /**
     * Whether or not Saucer is always on top of every other window.
     *
     * Set this value to change it.
     */
    alwaysOnTop: Promise<boolean> | boolean;

    /**
     * Whether or not Saucer has decorations (i.e the title bar).
     *
     * Set this value to change it.
     */
    decorated: Promise<boolean> | boolean;

    /**
     * Whether or not Saucer is in the foreground/focused.
     *
     * See also `focus()`.
     */
    isFocused: Promise<boolean> | boolean;

    /**
     * Focuses Saucer, bringing it into the foreground.
     *
     * See also `isFocused`.
     */
    focus(): Promise<void>;

    /**
     * The current size of the Saucer window.
     *
     * Set this value to change it.
     */
    size: Promise<SaucerSize> | SaucerSize;

    /**
     * The maximum allowed size of the Saucer window.
     *
     * Set this value to change it.
     */
    maxSize: Promise<SaucerSize> | SaucerSize;

    /**
     * The minimum allowed size of the Saucer window.
     *
     * Set this value to change it.
     */
    minSize: Promise<SaucerSize> | SaucerSize;

    /**
     * Whether or not Saucer is maximized.
     *
     * Set this value to `false` to restore the window to normal or `true` to maximize.
     */
    maximized: Promise<boolean> | boolean;

    /**
     * Whether or not Saucer is minimized.
     *
     * Set this value to `false` to restore the window to normal or `true` to minimize.
     */
    minimized: Promise<boolean> | boolean;

    /**
     * Whether or not Saucer is resizable by the user.
     *
     * Set this value to change it.
     */
    resizable: Promise<boolean> | boolean;

    /**
     * the title of the Saucer window.
     *
     * Set this value to change it.
     */
    title: Promise<string> | string;

    /**
     * Whether or not the context menu is enabled (i.e the right-click menu).
     *
     * Set this value to change it.
     */
    focus(): Promise<void>;

    /**
     * Hides Saucer, this causes the window to disappear from the taskbar and the
     * user will no longer be able to view the app no matter what they do.
     *
     * See also `show()`.
     */
    hide(): Promise<void>;

    /**
     * Unhides Saucer.
     *
     * See also `hide()`.
     */
    show(): Promise<void>;
};

export declare type SaucerIPCObject = {
    /**
     * Registers a listener for receiving mutation events from the main process.
     *
     * Note that this requires @JavascriptValue(watchForMutate = true) to work.
     */
    onMutate(
        propertyName: string,
        callback: (data: any) => void
    ): SaucerHandlerRegistrationId;

    /**
     * Unregisters a mutation listener.
     */
    offMutate(registrationId: SaucerHandlerRegistrationId): void;
};

declare global {
    interface Window {
        saucer: Saucer;
        svelte: (object: string, field: string) => Writable<null | any>;
    }
}

export { };