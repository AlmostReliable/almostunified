package com.almostreliable.unified.compat;

import com.almostreliable.unified.recipe.CRTLookup;
import com.almostreliable.unified.recipe.ClientRecipeTracker;
import com.almostreliable.unified.utils.Utils;
import me.shedaniel.math.Rectangle;
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
import me.shedaniel.rei.forge.REIPlugin;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@REIPlugin(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("UnstableApiUsage")
public class AlmostREI implements REIClientPlugin {

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
            private final ClientRecipeTracker.ClientRecipeLink link;

            private IndicatorView(DisplayCategoryView<Display> lastView, ClientRecipeTracker.ClientRecipeLink link) {
                this.lastView = lastView;
                this.link = link;
            }

            @Override
            public DisplayRenderer getDisplayRenderer(Display display) {
                return lastView.getDisplayRenderer(display);
            }

            @Override
            public List<Widget> setupDisplay(Display display, Rectangle bounds) {
                var widgets = lastView.setupDisplay(display, bounds);
                var area = calculateArea(bounds);
                widgets.add(Widgets.createDrawableWidget((helper, stack, mX, mY, delta) ->
                        RecipeIndicator.renderIndicator(stack, area)));
                var tooltipArea = new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight());
                widgets.add(Widgets.createTooltip(tooltipArea, RecipeIndicator.constructTooltip(link)));
                return widgets;
            }

            private Rect2i calculateArea(Rectangle bounds) {
                if (plusButtonArea != null) {
                    var area = plusButtonArea.get(bounds);
                    return new Rect2i(area.x, area.y - area.height - 2, area.width, area.height);
                }
                return new Rect2i(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }
}
