package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedFallbackRuntime;
import com.almostreliable.unified.ClientTagUpdateEvent;
import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.CRTLookup;
import com.almostreliable.unified.recipe.ClientRecipeTracker.ClientRecipeLink;
import com.almostreliable.unified.utils.Utils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AlmostREI implements REIClientPlugin {

    @Nullable private BasicFilteringRule.MarkDirty filterUpdate;

    public AlmostREI() {
        ClientTagUpdateEvent.register(() -> {
            if (filterUpdate != null) filterUpdate.markDirty();
        });
    }

    @Override
    public String getPluginProviderName() {
        return Utils.prefix(ModConstants.REI);
    }

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        filterUpdate = rule.hide(() -> {
            AlmostUnifiedFallbackRuntime.getInstance().reload();

            var reiDisabled = AlmostUnified
                    .getRuntime()
                    .getUnifyConfig()
                    .map(UnifyConfig::reiOrJeiDisabled)
                    .orElse(false);
            if (reiDisabled) return List.of();

            return EntryIngredients.ofItemStacks(HideHelper.createHidingList(AlmostUnified.getRuntime()));
        });
    }

    @Override
    public void postStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
        if (stage != ReloadStage.END || !manager.equals(PluginManager.getClientInstance())) return;
        CategoryRegistry.getInstance().forEach(category -> {
            IndicatorExtension extension = new IndicatorExtension(category.getPlusButtonArea().orElse(null));
            category.registerExtension(Utils.cast(extension));
        });
    }

    @SuppressWarnings("OverrideOnly")
    private record IndicatorExtension(@Nullable ButtonArea plusButtonArea)
            implements CategoryExtensionProvider<Display> {

        @Override
        public DisplayCategoryView<Display> provide(Display display, DisplayCategory<Display> category, DisplayCategoryView<Display> lastView) {
            return display
                    .getDisplayLocation()
                    .map(CRTLookup::getLink)
                    .map(link -> (DisplayCategoryView<Display>) new IndicatorView(lastView, link))
                    .orElse(lastView);
        }

        private final class IndicatorView implements DisplayCategoryView<Display> {

            private final DisplayCategoryView<Display> lastView;
            private final ClientRecipeLink link;

            private IndicatorView(DisplayCategoryView<Display> lastView, ClientRecipeLink link) {
                this.lastView = lastView;
                this.link = link;
            }

            @Override
            public DisplayRenderer getDisplayRenderer(Display display) {
                return lastView.getDisplayRenderer(display);
            }

            @Override
            public List<Widget> setupDisplay(Display display, Rectangle bounds) {
                int pX;
                int pY;
                int size;

                if (plusButtonArea != null) {
                    var area = plusButtonArea.get(bounds);
                    pX = area.x;
                    pY = area.y - area.height - 2;
                    size = area.width;
                } else {
                    pX = bounds.x - RecipeIndicator.RENDER_SIZE / 2;
                    pY = bounds.y - RecipeIndicator.RENDER_SIZE / 2;
                    size = RecipeIndicator.RENDER_SIZE;
                }

                var widgets = lastView.setupDisplay(display, bounds);
                widgets.add(Widgets.createDrawableWidget(
                        (guiGraphics, mX, mY, delta) -> RecipeIndicator.renderIndicator(guiGraphics, pX, pY, size)
                ));

                var tooltipArea = new Rectangle(pX, pY, size, size);
                widgets.add(Widgets.createTooltip(tooltipArea, RecipeIndicator.constructTooltip(link)));

                return widgets;
            }
        }
    }
}
