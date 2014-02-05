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
import org.terasology.anotherWorld.util.ChunkRandom;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.LivingPlantComponent;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.entity.neighbourUpdate.LargeBlockUpdateFinished;
import org.terasology.world.block.entity.neighbourUpdate.LargeBlockUpdateStarting;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.chunks.ChunkConstants;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AdvancedLSystemTreeDefinition {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedLSystemTreeDefinition.class);
    private static final long FAILED_GROWTH_INTERVAL = 1000;

    private Map<Character, AxionElementGeneration> blockMap;
    private Map<Character, AxionElementReplacement> axionElementReplacements;
    private PDist branchAngle;
    private PDist treeLongevity;
    private int growthInterval;
    private String treeType;
    private String saplingAxion;

    public AdvancedLSystemTreeDefinition(String treeType, String saplingAxion, Map<Character, AxionElementReplacement> axionElementReplacements,
                                         Map<Character, AxionElementGeneration> blockMap, float angle) {
        this(treeType, saplingAxion, new PDist(angle, (float) Math.PI / 18f), new PDist(37, 7), 120 * 1000, axionElementReplacements,
                blockMap);
    }

    public AdvancedLSystemTreeDefinition(String treeType, String saplingAxion, PDist branchAngle, PDist treeLongevity, int growthInterval,
                                         Map<Character, AxionElementReplacement> axionElementReplacements, Map<Character, AxionElementGeneration> blockMap) {
        this.treeType = treeType;
        this.saplingAxion = saplingAxion;
        this.branchAngle = branchAngle;
        this.treeLongevity = treeLongevity;
        this.growthInterval = growthInterval;
        this.axionElementReplacements = axionElementReplacements;
        this.blockMap = blockMap;
    }

    public void generateTree(String seed, String saplingBlock, Vector3i chunkPos, ChunkView chunkView, int x, int y, int z) {
        Vector3i worldTreeLocation = chunkView.toWorldPos(new Vector3i(x, y, z));
        LSystemTreeComponent treeComponent = createNewTreeComponent(seed, worldTreeLocation);

        // Block locations in tree base coordinate system
        Map<Vector3i, TreeBlockDefinition> treeBlocks = generateTreeFromAxiom(treeComponent.axion, treeComponent.branchAngle, treeComponent.rotationAngle);

        int chunkStartX = chunkPos.x * ChunkConstants.SIZE_X;
        int chunkStartY = chunkPos.y * ChunkConstants.SIZE_Y;
        int chunkStartZ = chunkPos.z * ChunkConstants.SIZE_Z;

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        for (Map.Entry<Vector3i, TreeBlockDefinition> treeBlock : treeBlocks.entrySet()) {
            Vector3i blockLocation = treeBlock.getKey();

            // Do not set the base block - it will have to be initialized
            if (!blockLocation.equals(Vector3i.zero())) {
                TreeBlockDefinition blockDefinition = treeBlock.getValue();
                Block block = getBlock(blockManager, blockDefinition, blockLocation, treeBlocks.keySet());
                chunkView.setBlock(
                        worldTreeLocation.x + blockLocation.x - chunkStartX,
                        worldTreeLocation.y + blockLocation.y - chunkStartY,
                        worldTreeLocation.z + blockLocation.z - chunkStartZ,
                        block);
            }
        }

        Block sapling = blockManager.getBlock(saplingBlock);
        chunkView.setBlock(worldTreeLocation.x - chunkStartX, worldTreeLocation.y - chunkStartY, worldTreeLocation.z - chunkStartZ, sapling);
    }

    public Long setupTreeBaseBlock(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef sapling) {
        Vector3i location = sapling.getComponent(BlockComponent.class).getPosition();

        LSystemTreeComponent treeComponent = createNewTreeComponent(worldProvider.getSeed(), location);

        FastRandom rand = new FastRandom();

        // Update time when sapling was placed
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();
        treeComponent.lastGrowthTime = time - rand.nextInt(growthInterval);

        Map<Vector3i, TreeBlockDefinition> treeBlocks = generateTreeFromAxiom(treeComponent.axion, treeComponent.branchAngle, treeComponent.rotationAngle);

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        TreeBlockDefinition blockDefinition = treeBlocks.get(Vector3i.zero());
        Block baseBlock = getBlock(blockManager, blockDefinition, Vector3i.zero(), treeBlocks.keySet());

        worldProvider.setBlock(location, baseBlock);

        checkForDeathAndSetupComponents(blockEntityRegistry, rand, location, treeComponent);

        return (long) growthInterval;
    }

    private LSystemTreeComponent createNewTreeComponent(String seed, Vector3i location) {
        Random random = ChunkRandom.getChunkRandom(seed, location, 345245);

        // New axion (grown)
        int generation = 1 + random.nextInt((int) treeLongevity.getMax() - 1);
        String nextAxion = saplingAxion;
        for (int i = 0; i < generation; i++) {
            nextAxion = generateNextAxion(random, nextAxion);
        }

        LSystemTreeComponent lSystemTree = new LSystemTreeComponent();
        lSystemTree.axion = nextAxion;
        lSystemTree.branchAngle = branchAngle.getIntValue(random);
        lSystemTree.rotationAngle = (float) Math.PI * random.nextFloat();
        lSystemTree.generation = generation;
        return lSystemTree;
    }

    public Long updateTree(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef treeRef) {
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();
        if (lSystemTree.lastGrowthTime == 0) {
            // This tree was just planted
            lSystemTree.lastGrowthTime = time;
            treeRef.saveComponent(lSystemTree);

            return (long) growthInterval;
        } else {
            Vector3i treeLocation = treeRef.getComponent(BlockComponent.class).getPosition();
            if (hasRoomToGrow(worldProvider, treeLocation)) {

                FastRandom rand = new FastRandom();

                String oldAxion = lSystemTree.axion;
                String nextAxion = generateNextAxion(rand, oldAxion);

                lSystemTree.axion = nextAxion;
                lSystemTree.generation++;
                lSystemTree.lastGrowthTime = time;

                return replaceTreeAndCheckForDeath(worldProvider, blockEntityRegistry, rand, treeLocation, oldAxion, nextAxion, lSystemTree);
            }
            return (long) growthInterval;
        }
    }

    private Long replaceTreeAndCheckForDeath(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Random random,
                                             Vector3i location, String oldAxion, String nextAxion, LSystemTreeComponent treeComponent) {
        Map<Vector3i, TreeBlockDefinition> currentTree = generateTreeFromAxiom(oldAxion, treeComponent.branchAngle, treeComponent.rotationAngle);
        Map<Vector3i, TreeBlockDefinition> nextTree = generateTreeFromAxiom(nextAxion, treeComponent.branchAngle, treeComponent.rotationAngle);

        if (!updateTreeInGame(worldProvider, blockEntityRegistry, location, currentTree, nextTree)) {
            return FAILED_GROWTH_INTERVAL;
        }

        return checkForDeathAndSetupComponents(blockEntityRegistry, random, location, treeComponent);
    }

    private Long checkForDeathAndSetupComponents(BlockEntityRegistry blockEntityRegistry, Random random, Vector3i location, LSystemTreeComponent treeComponent) {
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
            return (long) growthInterval;
        } else {
            if (entity.hasComponent(LivingPlantComponent.class)) {
                entity.removeComponent(LivingPlantComponent.class);
                entity.removeComponent(LSystemTreeComponent.class);
            }
            return null;
        }
    }

    private boolean hasRoomToGrow(WorldProvider worldProvider, Vector3i treeLocation) {
        return worldProvider.isBlockRelevant(treeLocation.x + ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z + ChunkConstants.SIZE_Z)
                && worldProvider.isBlockRelevant(treeLocation.x + ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z)
                && worldProvider.isBlockRelevant(treeLocation.x + ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z - ChunkConstants.SIZE_Z)
                && worldProvider.isBlockRelevant(treeLocation.x, treeLocation.y, treeLocation.z + ChunkConstants.SIZE_Z)
                && worldProvider.isBlockRelevant(treeLocation.x, treeLocation.y, treeLocation.z - ChunkConstants.SIZE_Z)
                && worldProvider.isBlockRelevant(treeLocation.x - ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z + ChunkConstants.SIZE_Z)
                && worldProvider.isBlockRelevant(treeLocation.x - ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z)
                && worldProvider.isBlockRelevant(treeLocation.x - ChunkConstants.SIZE_X, treeLocation.y, treeLocation.z - ChunkConstants.SIZE_Z);
    }

    private boolean checkForDeath(int generation, float random) {
        if (generation < treeLongevity.getMin()) {
            return false;
        }
        double deathChance = Math.pow(1f * (treeLongevity.getMax() - generation) / treeLongevity.range, 0.2);
        return (deathChance < random);
    }

    private Block getBlock(BlockManager blockManager, TreeBlockDefinition block, Vector3i location, Collection<Vector3i> treeBlocks) {
        BlockUri blockFamilyUri = new BlockUri(block.getBlockUri());
        if (block.isBranchBlock()) {
            byte connections = 0;
            for (Side connectSide : SideBitFlag.getSides((byte) 63)) {
                Vector3i neighborLocation = new Vector3i(location);
                neighborLocation.add(connectSide.getVector3i());

                if (treeBlocks.contains(neighborLocation)) {
                    connections += SideBitFlag.getSide(connectSide);
                }
            }
            return blockManager.getBlock(new BlockUri(blockFamilyUri, String.valueOf(connections)));
        } else {
            return blockManager.getBlock(blockFamilyUri);
        }
    }

    private boolean updateTreeInGame(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i treeLocation,
                                     Map<Vector3i, TreeBlockDefinition> currentTree, Map<Vector3i, TreeBlockDefinition> nextTree) {
        Block air = BlockManager.getAir();
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        int replaceCount = 0;
        Map<Vector3i, Block> blocksToReplaceExistingTreeBlocks = new HashMap<>();
        Map<Vector3i, Block> blocksToPlaceInNewPlaces = new HashMap<>();

        EntityRef worldEntity = worldProvider.getWorldEntity();

        for (Map.Entry<Vector3i, TreeBlockDefinition> newTreeBlock : nextTree.entrySet()) {
            Vector3i relativeLocation = newTreeBlock.getKey();
            TreeBlockDefinition oldBlock = currentTree.remove(relativeLocation);
            TreeBlockDefinition newBlock = newTreeBlock.getValue();

            Vector3i blockLocation = new Vector3i(treeLocation.x + relativeLocation.x, treeLocation.y + relativeLocation.y, treeLocation.z + relativeLocation.z);
            Block resultBlock = getBlock(blockManager, newBlock, relativeLocation, nextTree.keySet());

            if (oldBlock != null && !oldBlock.equals(newBlock)) {
                Block block = worldProvider.getBlock(blockLocation);
                if (block.isReplacementAllowed() || oldBlock == newBlock) {
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
                for (Vector3i location : currentTree.keySet()) {
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

    private String generateNextAxion(Random rand, String currentAxion) {
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

    private Map<Vector3i, TreeBlockDefinition> generateTreeFromAxiom(String currentAxion, float angle, float treeRotation) {
        Map<Vector3i, TreeBlockDefinition> treeInMemory = Maps.newHashMap();

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
                    tempRotation.rotX(angle);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setIdentity();
                    tempRotation.rotX(-angle);
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

    private void setBlock(Map<Vector3i, TreeBlockDefinition> treeInMemory, Vector3f position, TreeBlockDefinition treeBlockDefinition) {
        Vector3i blockPosition = new Vector3i(position.x + 0.5f, position.y + 0.5f, position.z + 0.5f);
        if (blockPosition.y >= 0) {
            final TreeBlockDefinition blockAtPosition = treeInMemory.get(blockPosition);
            if ((blockAtPosition != null && blockAtPosition.equals(treeBlockDefinition)) || hasBlockWithHigherPriority(treeBlockDefinition, blockAtPosition)) {
                return;
            }
            treeInMemory.put(blockPosition, treeBlockDefinition);
        }
    }

    private boolean hasBlockWithHigherPriority(TreeBlockDefinition block, TreeBlockDefinition blockAtPosition) {
        return blockAtPosition != null && blockAtPosition.getTreePart().getPriority() > block.getTreePart().getPriority();
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
        private Map<Vector3i, TreeBlockDefinition> treeInMemory;
        private Vector3f position;
        private Matrix4f rotation;

        private Callback(Map<Vector3i, TreeBlockDefinition> treeInMemory, Vector3f position, Matrix4f rotation) {
            this.treeInMemory = treeInMemory;
            this.position = position;
            this.rotation = rotation;
        }

        @Override
        public void setBlock(Vector3f blockPosition, TreeBlockDefinition blockDefinition) {
            AdvancedLSystemTreeDefinition.this.setBlock(treeInMemory, blockPosition, blockDefinition);
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
