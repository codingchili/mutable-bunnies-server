/**
 * Plays sound effects.
 *
 * - volume should be based on distance.
 * - make sure sounds are cached.
 * - support background audio from realm config.
 * - footsteps, spell effects etc.
 *
 * @type {Window.SoundManager}
 */
window.SoundManager = class SoundManager {
    play(url) {
        let effect = new Audio(`/resources/sound/${url}`);
        effect.volume = 0.4;

        effect.addEventListener('loadeddata', () => {
            effect.play();
        });
    }
};

var sound = new SoundManager();