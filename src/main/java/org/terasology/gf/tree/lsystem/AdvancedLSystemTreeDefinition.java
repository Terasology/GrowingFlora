/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.tree.lsystem;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.tree.LivingPlantComponent;
import org.terasology.gf.tree.PlantDefinition;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.neighbourUpdate.LargeBlockUpdateFinished;
import org.terasology.world.block.entity.neighbourUpdate.LargeBlockUpdateStarting;
import org.terasology.world.block.entity.placement.PlaceBlocks;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AdvancedLSystemTreeDefinition implements PlantDefinition {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedLSystemTreeDefinition.class);

    private static final int GROWTH_SAFE_DISTANCE = 25;
    private static final float MAX_ANGLE_OFFSET = (float) Math.PI / 18f;
    private static final int GROWTH_INTERVAL = 120 * 1000;

    private Map<Character, AxionElementGeneration> blockMap;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private List<String> blockPriorities;
    private float angle;
    private int minGenerations = 30;
    private int maxGenerations = 45;
    private String treeType;
    private String saplingAxion;

    public AdvancedLSystemTreeDefinition(String treeType, String saplingAxion, Map<Character, AxionElementReplacement> axionElementReplacements,
                                         Map<Character, AxionElementGeneration> blockMap, List<String> blockPriorities, float angle) {
        this.treeType = treeType;
        this.saplingAxion = saplingAxion;
        this.axionElementReplacements = axionElementReplacements;
        this.blockMap = blockMap;
        this.blockPriorities = blockPriorities;
        this.angle = angle;
    }

    @Override
    public boolean initializeSapling(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef sapling) {
        Vector3i location = sapling.getComponent(BlockComponent.class).getPosition();
        if (!hasRoomToGrow(worldProvider, location)) {
            return false;
        }

        FastRandom rand = new FastRandom();
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();

        // Old axion (generated)
        String oldAxion = saplingAxion;

        // New axion (grown)
        int generation = 1 + rand.nextInt(maxGenerations - 1);
        String nextAxion = oldAxion;
        for (int i = 0; i < generation; i++) {
            nextAxion = generateNextAxion(rand, nextAxion);
        }

        LSystemTreeComponent lSystemTree = new LSystemTreeComponent();
        lSystemTree.axion = nextAxion;
        lSystemTree.branchAngle = rand.nextFloat(-MAX_ANGLE_OFFSET, MAX_ANGLE_OFFSET);
        lSystemTree.rotationAngle = (float) Math.PI * rand.nextFloat();
        lSystemTree.generation = generation;
        // Update time when sapling was placed
        lSystemTree.lastGrowthTime = time + rand.nextInt(GROWTH_INTERVAL);

        return replaceTreeAndCheckForDeath(worldProvider, blockEntityRegistry, rand, location, oldAxion, nextAxion, lSystemTree);
    }

    @Override
    public void updatePlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef treeRef) {
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();
        if (shouldProcessTreeGrowth(lSystemTree, time)) {
            Vector3i treeLocation = treeRef.getComponent(BlockComponent.class).getPosition();
            if (hasRoomToGrow(worldProvider, treeLocation)) {

                FastRandom rand = new FastRandom();

                String oldAxion = lSystemTree.axion;
                String nextAxion = generateNextAxion(rand, oldAxion);

                lSystemTree.axion = nextAxion;
                lSystemTree.generation++;
                lSystemTree.lastGrowthTime = time;

                replaceTreeAndCheckForDeath(worldProvider, blockEntityRegistry, rand, treeLocation, oldAxion, nextAxion, lSystemTree);
            }
        }
    }

    private boolean replaceTreeAndCheckForDeath(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Random random,
                                                Vector3i location, String oldAxion, String nextAxion, LSystemTreeComponent treeComponent) {
        Map<Vector3i, String> currentTree = generateTreeFromAxiom(oldAxion, treeComponent.branchAngle, treeComponent.rotationAngle);
        Map<Vector3i, String> nextTree = generateTreeFromAxiom(nextAxion, treeComponent.branchAngle, treeComponent.rotationAngle);

        if (!updateTreeInGame(worldProvider, blockEntityRegistry, location, currentTree, nextTree)) {
            return false;
        }

        EntityRef entity = blockEntityRegistry.getBlockEntityAt(location);
        if (!checkForDeath(treeComponent.generation, random.nextFloat())) {
            if (entity.hasComponent(LSystemTreeComponent.class)) {
                entity.saveComponent(treeComponent);
            } else {
                entity.addComponent(treeComponent);
            }
            if (!entity.hasComponent(LivingPlantComponent.class)) {
                LivingPlantComponent livingPlantComponent = new LivingPlantComponent();
                livingPlantComponent.type = treeType;
                entity.addComponent(livingPlantComponent);
            }
        } else {
            if (entity.hasComponent(LivingPlantComponent.class)) {
                entity.removeComponent(LivingPlantComponent.class);
                entity.removeComponent(LSystemTreeComponent.class);
            }
        }

        return true;
    }

    private boolean shouldProcessTreeGrowth(LSystemTreeComponent lSystemTree, long time) {
        logger.debug("Considering processing tree, last growth: " + lSystemTree.lastGrowthTime + ", current time: " + time);
        return lSystemTree.lastGrowthTime + GROWTH_INTERVAL < time;
    }

    private boolean hasRoomToGrow(WorldProvider worldProvider, Vector3i treeLocation) {
        return worldProvider.isBlockRelevant(treeLocation.x + GROWTH_SAFE_DISTANCE, treeLocation.y, treeLocation.z + GROWTH_SAFE_DISTANCE)
                && worldProvider.isBlockRelevant(treeLocation.x - GROWTH_SAFE_DISTANCE, treeLocation.y, treeLocation.z - GROWTH_SAFE_DISTANCE);
    }

    private boolean checkForDeath(int generation, float random) {
        if (generation < minGenerations) {
            return false;
        }
        double deathChance = Math.pow(1f * (maxGenerations - generation) / (maxGenerations - minGenerations), 0.2);
//        logger.debug("Death chance: " + ((1 - deathChance) * 100) + "%");
        return (deathChance < random);
    }

    private boolean updateTreeInGame(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i treeLocation,
                                     Map<Vector3i, String> currentTree, Map<Vector3i, String> nextTree) {
        Block air = BlockManager.getAir();
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        int replaceCount = 0;
        Map<Vector3i, Block> blocksToReplaceExistingTreeBlocks = new HashMap<>();
        Map<Vector3i, Block> blocksToPlaceInNewPlaces = new HashMap<>();

        EntityRef worldEntity = CoreRegistry.get(EntityManager.class).getEntitiesWith(WorldComponent.class).iterator().next();

        for (Map.Entry<Vector3i, String> newTreeBlock : nextTree.entrySet()) {
            Vector3i relativeLocation = newTreeBlock.getKey();
            String oldBlock = currentTree.remove(relativeLocation);
            String newBlock = newTreeBlock.getValue();

            Vector3i blockLocation = new Vector3i(treeLocation.x + relativeLocation.x, treeLocation.y + relativeLocation.y, treeLocation.z + relativeLocation.z);
            Block resultBlock = blockManager.getBlockFamily(newBlock).getBlockForPlacement(worldProvider, blockEntityRegistry, blockLocation, null, null);

            if (oldBlock != null && !oldBlock.equals(newBlock)) {
                Block block = worldProvider.getBlock(blockLocation);
                if (block.isReplacementAllowed() || block.getBlockFamily() == blockManager.getBlockFamily(oldBlock)) {
                    blocksToReplaceExistingTreeBlocks.put(blockLocation, resultBlock);
                    replaceCount++;
                }
            } else if (oldBlock == null) {
                if (worldProvider.getBlock(blockLocation).isReplacementAllowed()) {
                    blocksToPlaceInNewPlaces.put(blockLocation, resultBlock);
                    replaceCount++;
                }
            }
        }

        worldEntity.send(new LargeBlockUpdateStarting());
        try {
            PlaceBlocks placeBlocks = new PlaceBlocks(blocksToPlaceInNewPlaces);
            worldProvider.getWorldEntity().send(placeBlocks);

            if (!placeBlocks.isConsumed()) {
                for (Map.Entry<Vector3i, String> oldTreeBlock : currentTree.entrySet()) {
                    Vector3i location = oldTreeBlock.getKey();
                    // Remove the old block of tree
                    blocksToReplaceExistingTreeBlocks.put(new Vector3i(treeLocation.x + location.x, treeLocation.y + location.y, treeLocation.z + location.z), air);
                    replaceCount++;
                }

                for (Map.Entry<Vector3i, Block> blockLocation : blocksToReplaceExistingTreeBlocks.entrySet()) {
                    worldProvider.setBlock(blockLocation.getKey(), blockLocation.getValue());
                }

                logger.debug("Replaced block count: " + replaceCount);

                return true;
            }
            return false;
        } finally {
            worldEntity.send(new LargeBlockUpdateFinished());
        }
    }

    private String generateNextAxion(FastRandom rand, String currentAxion) {
        StringBuilder result = new StringBuilder();
        for (AxionElement axion : parseAxions(currentAxion)) {
            final AxionElementReplacement axionElementReplacement = axionElementReplacements.get(axion.key);
            if (axionElementReplacement != null) {
                result.append(axionElementReplacement.getReplacement(rand.nextFloat(), currentAxion));
            } else {
                result.append(axion.key);
                if (axion.parameter != null) {
                    result.append("(").append(axion.parameter).append(")");
                }
            }
        }

        return result.toString();
    }

    private Map<Vector3i, String> generateTreeFromAxiom(String currentAxion, float angleOffset, float treeRotation) {
        Map<Vector3i, String> treeInMemory = Maps.newHashMap();

        Deque<Vector3f> stackPosition = Queues.newArrayDeque();
        Deque<Matrix4f> stackOrientation = Queues.newArrayDeque();

        Vector3f position = new Vector3f(0, 0, 0);
        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        rotation.rotY(treeRotation);

        Callback callback = new Callback(treeInMemory, position, rotation);

        for (AxionElement axion : parseAxions(currentAxion)) {
            Matrix4f tempRotation = new Matrix4f();
            tempRotation.setIdentity();

            char c = axion.key;
            switch (c) {
                case '[':
                    stackOrientation.push(new Matrix4f(rotation));
                    stackPosition.push(new Vector3f(position));
                    break;
                case ']':
                    rotation.set(stackOrientation.pop());
                    position.set(stackPosition.pop());
                    break;
                case '&':
                    tempRotation.setIdentity();
                    tempRotation.rotX(angle + angleOffset);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.rotX(-angle - angleOffset);
                    rotation.mul(tempRotation);
                    break;
                case '+':
                    tempRotation.setIdentity();
                    tempRotation.rotY((float) Math.toRadians(Integer.parseInt(axion.parameter)));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setIdentity();
                    tempRotation.rotY(-(float) Math.toRadians(Integer.parseInt(axion.parameter)));
                    rotation.mul(tempRotation);
                    break;
                default:
                    AxionElementGeneration axionElementGeneration = blockMap.get(c);
                    if (axionElementGeneration != null) {
                        axionElementGeneration.generate(callback, position, rotation, axion.parameter);
                    }
            }
        }
        return treeInMemory;
    }

    private void setBlock(Map<Vector3i, String> treeInMemory, Vector3f position, String blockUri) {
        Vector3i blockPosition = new Vector3i(position.x + 0.5f, position.y + 0.5f, position.z + 0.5f);
        if (blockPosition.y >= 0) {
            final String blockAtPosition = treeInMemory.get(blockPosition);
            if ((blockAtPosition != null && blockAtPosition.equals(blockUri)) || hasBlockWithHigherPriority(blockUri, blockAtPosition)) {
                return;
            }
            treeInMemory.put(blockPosition, blockUri);
        }
    }

    private boolean hasBlockWithHigherPriority(String block, String blockAtPosition) {
        return blockAtPosition != null && blockPriorities.indexOf(blockAtPosition) < blockPriorities.indexOf(block);
    }

    private static List<AxionElement> parseAxions(String axionString) {
        List<AxionElement> result = new LinkedList<>();
        char[] chars = axionString.toCharArray();
        int index = 0;
        int size = chars.length;
        while (index < size) {
            char c = chars[index];
            if (c == '(' || c == ')') {
                throw new IllegalArgumentException("Invalid axion - parameter without key");
            }
            if (index + 1 < size && chars[index + 1] == '(') {
                int closingBracket = axionString.indexOf(')', index + 1);
                if (closingBracket < 0) {
                    throw new IllegalArgumentException("Invalid axion - missing closing bracket");
                }
                String parameter = axionString.substring(index + 2, closingBracket);
                index = closingBracket;
                result.add(new AxionElement(c, parameter));
            } else {
                result.add(new AxionElement(c));
            }
            index++;
        }

        return result;
    }

    private final class Callback implements AxionElementGeneration.AxionElementGenerationCallback {
        private Map<Vector3i, String> treeInMemory;
        private Vector3f position;
        private Matrix4f rotation;

        private Callback(Map<Vector3i, String> treeInMemory, Vector3f position, Matrix4f rotation) {
            this.treeInMemory = treeInMemory;
            this.position = position;
            this.rotation = rotation;
        }

        @Override
        public void setBlock(Vector3f blockPosition, String blockUri) {
            AdvancedLSystemTreeDefinition.this.setBlock(treeInMemory, blockPosition, blockUri);
        }

        @Override
        public void advance(float distance) {
            Vector3f dir = new Vector3f(0, distance, 0);
            rotation.transform(dir);
            position.add(dir);
        }
    }

    private static final class AxionElement {
        private char key;
        private String parameter;

        private AxionElement(char key, String parameter) {
            this.key = key;
            this.parameter = parameter;
        }

        private AxionElement(char key) {
            this.key = key;
        }
    }
}
