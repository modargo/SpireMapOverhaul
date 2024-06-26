package spireMapOverhaul.zones.manasurge.ui.campfire;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import spireMapOverhaul.zones.manasurge.ManaSurgeZone;
import spireMapOverhaul.zones.manasurge.vfx.EnchantBlightEffect;

public class CampfireEnchantEffect extends AbstractGameEffect {
    private static final float DUR = 1.5F;
    private boolean openedScreen = false;
    private Color screenColor;

    public CampfireEnchantEffect() {
        this.screenColor = AbstractDungeon.fadeColor.cpy();
        this.duration = DUR;
        this.screenColor.a = 0.0F;
        AbstractDungeon.overlayMenu.proceedButton.hide();
    }

    public void update() {
        if (!AbstractDungeon.isScreenUp) {
            this.duration -= Gdx.graphics.getDeltaTime();
            this.updateBlackScreenColor();
        }

        if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
                ManaSurgeZone.applyPermanentPositiveModifier(c);
                AbstractDungeon.player.bottledCardUpgradeCheck(c);
                CardCrawlGame.sound.play(ManaSurgeZone.ENCHANTBLIGHT_KEY);
                AbstractDungeon.topLevelEffectsQueue.add(new EnchantBlightEffect((float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectsQueue.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy(), (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
            }

            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            ((RestRoom) AbstractDungeon.getCurrRoom()).fadeIn();
        }

        if (this.duration < 1.0F && !this.openedScreen) {
            this.openedScreen = true;
            CardGroup selectedCards = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            AbstractDungeon.player.masterDeck.group.stream()
                    .filter(card -> !ManaSurgeZone.hasManaSurgeModifier(card) &&
                            card.cost != -2 &&
                            card.type != AbstractCard.CardType.CURSE &&
                            card.type != AbstractCard.CardType.STATUS)
                    .forEach(selectedCards::addToTop);

            AbstractDungeon.gridSelectScreen.open(
                    selectedCards,
                    1,
                    EnchantOption.TEXT[2],
                    false,
                    false,
                    true,
                    true
            );
        }

        if (this.duration < 0.0F) {
            this.isDone = true;
            if (CampfireUI.hidden) {
                AbstractRoom.waitTimer = 0.0F;
                AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                ((RestRoom) AbstractDungeon.getCurrRoom()).cutFireSound();
            }
        }

    }

    private void updateBlackScreenColor() {
        if (this.duration > 1.0F) {
            this.screenColor.a = Interpolation.fade.apply(1.0F, 0.0F, (this.duration - 1.0F) * 2.0F);
        } else {
            this.screenColor.a = Interpolation.fade.apply(0.0F, 1.0F, this.duration / 1.5F);
        }

    }

    public void render(SpriteBatch sb) {
        sb.setColor(this.screenColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, (float)Settings.WIDTH, (float)Settings.HEIGHT);
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            AbstractDungeon.gridSelectScreen.render(sb);
        }

    }

    public void dispose() {
    }
}