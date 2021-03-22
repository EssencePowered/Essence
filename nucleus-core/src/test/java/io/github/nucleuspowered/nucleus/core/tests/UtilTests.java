/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.tests;

import io.github.nucleuspowered.nucleus.core.Util;
import org.junit.Ignore;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.spongepowered.api.world.WorldBorder;

import java.time.Duration;
import java.util.Arrays;

public class UtilTests {

    @SuppressWarnings("CanBeFinal")
    @RunWith(Parameterized.class)
    public static class WorldBorderTests {
        @Parameterized.Parameters(name = "{index}: Co-ords ({0}, {1}, {2}), border centre ({3}, {4}, {5}), diameter: {6}, expecting {7}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {0, 0, 0, 0, 0, 0, 10, true},
                    {20, 0, 0, 0, 0, 0, 10, false},
                    {0, 20, 0, 0, 0, 0, 10, true},
                    {20, 0, 20, 0, 0, 0, 10, false},
                    {0, 0, 20, 0, 0, 0, 10, false},
                    {4, 0, 4, 0, 0, 0, 10, true},
                    {5, 0, 5, 0, 0, 0, 10, true},
                    {5, 0, 5, 0, 20, 0, 10, true},
                    {6, 0, 5, 0, 20, 0, 10, false},
                    {5, 0, 5, 500, 0, 0, 10, false},
                    {499, 0, 5, 500, 0, 0, 10, true},
                    {499, 0, 500, 500, 0, 0, 10, false}
            });
        }

        @Parameterized.Parameter()
        public double x;

        @Parameterized.Parameter(1)
        public double y;

        @Parameterized.Parameter(2)
        public double z;

        @Parameterized.Parameter(3)
        public double borderX;

        @Parameterized.Parameter(4)
        public double borderY;

        @Parameterized.Parameter(5)
        public double borderZ;

        @Parameterized.Parameter(6)
        public double dia;

        @Parameterized.Parameter(7)
        public boolean result;

        private WorldBorder getBorder() {
            return new WorldBorder() {


                @Override public double newDiameter() {
                    return 0;
                }

                @Override public double diameter() {
                    return 0;
                }

                @Override public void setDiameter(final double diameter) {

                }

                @Override public void setDiameter(final double diameter, final Duration duration) {

                }

                @Override public void setDiameter(final double startDiameter, final double endDiameter, final Duration duration) {

                }

                @Override public Duration timeRemaining() {
                    return null;
                }

                @Override public void setCenter(final double x, final double z) {

                }

                @Override public Vector3d center() {
                    return null;
                }

                @Override public Duration warningTime() {
                    return null;
                }

                @Override public void setWarningTime(final Duration time) {

                }

                @Override public double warningDistance() {
                    return 0;
                }

                @Override public void setWarningDistance(final double distance) {

                }

                @Override public double damageThreshold() {
                    return 0;
                }


                @Override public void setDamageThreshold(final double distance) {

                }

                @Override public double damageAmount() {
                    return 0;
                }

                @Override public void setDamageAmount(final double damage) {

                }
            };
        }

        @Test
        @Ignore
        public void testInWorldBorder() {
            final WorldBorder wb = this.getBorder();
            final ServerWorld world = Mockito.mock(ServerWorld.class);
            Mockito.when(world.properties().worldBorder()).thenReturn(wb);

            final ServerLocation lw = Mockito.mock(ServerLocation.class);
            Mockito.when(lw.world()).thenReturn(world);
            Mockito.when(lw.position()).thenReturn(new Vector3d(this.x, this.y, this.z));
            Assert.assertEquals(this.result, Util.isLocationInWorldBorder(lw));
        }
    }
}
