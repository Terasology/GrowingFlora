// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import com.google.common.collect.Queues;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.SideBitFlag;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.entity.neighbourUpdate.LargeBlockUpdateFinished;
import org.terasology.engine.world.block.entity.neighbourUpdate.LargeBlockUpdateStarting;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.gf.LivingPlantComponent;
import org.terasology.gestalt.naming.Name;
import org.terasology.utilities.procedural.PDist;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdvancedLSystemTreeDefinition {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedLSystemTreeDefinition.class);
    private static final long FAILED_GROWTH_INTERVAL = 10000;

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

    public void generateTree(long seed, String saplingBlock, Chunk chunk, int x, int y, int z) {
        Vector3i worldPos = new Vector3i(x, y, z);
        LSystemTreeComponent treeComponent = createNewTreeComponent(seed, worldPos);

        // Block locations in world coordinates
        Map<Vector3i, TreeBlockDefinition> treeBlocks = generateTreeFromAxion(worldPos, treeComponent.axion, treeComponent.branchAngle, treeComponent.rotationAngle)
                .gatherBlockDefinitions();

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);

        for (Map.Entry<Vector3i, TreeBlockDefinition> treeBlock : treeBlocks.entrySet()) {
            Vector3i blockLocation = treeBlock.getKey();

            // Do not set the base block - it will have to be initialized from the sapling
            if (!blockLocation.equals(worldPos)) {
                TreeBlockDefinition blockDefinition = treeBlock.getValue();
                Block block = getBlock(blockManager, blockDefinition, blockLocation, treeBlocks.keySet());
                if (chunk.getRegion().contains(blockLocation)) {
                    chunk.setBlock(Chunks.toRelative(blockLocation, new Vector3i()), block);
                }
            }
        }

        if (chunk.getRegion().contains(worldPos)) {
            Block sapling = blockManager.getBlock(saplingBlock);
            chunk.setBlock(Chunks.toRelative(worldPos, new Vector3i()), sapling);
        }
    }

    public Long setupTreeBaseBlock(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef sapling) {
        Vector3i location = sapling.getComponent(BlockComponent.class).getPosition(new Vector3i());

        LSystemTreeComponent treeComponent = createNewTreeComponent(worldProvider.getSeed().hashCode(), location);

        FastRandom rand = new FastRandom();

        // Update time when sapling was placed
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();
        int growthWait = rand.nextInt(growthInterval);
        treeComponent.lastGrowthTime = time - growthWait;

        Map<Vector3i, TreeBlockDefinition> treeBlocks = generateTreeFromAxion(location, treeComponent.axion, treeComponent.branchAngle, treeComponent.rotationAngle)
                .gatherBlockDefinitions();

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        TreeBlockDefinition blockDefinition = treeBlocks.get(location);
        Block baseBlock = getBlock(blockManager, blockDefinition, location, treeBlocks.keySet());

        worldProvider.setBlock(location, baseBlock);

        checkForDeathAndSetupComponents(blockEntityRegistry, rand, location, treeComponent);

        return (long) growthWait;
    }

    private LSystemTreeComponent createNewTreeComponent(long seed, Vector3ic location) {
        Random random = new FastRandom(seed + 345245 * (97L * location.x() + 13L * location.y() + location.z()));

        // New axion (grown)
        int generation = 1 + random.nextInt((int) treeLongevity.getMax() - 1);
        String nextAxion = saplingAxion;
        for (int i = 0; i < generation; i++) {
            nextAxion = generateNextAxion(random, nextAxion);
        }

        LSystemTreeComponent lSystemTree = new LSystemTreeComponent();
        lSystemTree.axion = nextAxion;
        lSystemTree.branchAngle = branchAngle.getValue(random);
        lSystemTree.rotationAngle = (float) Math.PI * random.nextFloat();
        lSystemTree.generation = generation;
        return lSystemTree;
    }

    public Long setupPlantedSapling(EntityRef treeRef) {
        Random random = new FastRandom();
        long time = CoreRegistry.get(Time.class).getGameTimeInMs();

        // New axion (grown)
        int generation = 1;

        LSystemTreeComponent lSystemTree = new LSystemTreeComponent();
        lSystemTree.axion = saplingAxion;
        lSystemTree.branchAngle = branchAngle.getValue(random);
        lSystemTree.rotationAngle = (float) Math.PI * random.nextFloat();
        lSystemTree.generation = generation;
        // This tree was just planted
        lSystemTree.lastGrowthTime = time;
        treeRef.addComponent(lSystemTree);

        return (long) growthInterval;
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
            Vector3i treeLocation = treeRef.getComponent(BlockComponent.class).getPosition(new Vector3i());
            TreeStructure oldTreeStructure = generateTreeFromAxion(treeLocation, lSystemTree.axion, lSystemTree.branchAngle, lSystemTree.rotationAngle);
            if (isWholeTreeSpaceLoaded(worldProvider, oldTreeStructure)) {
                FastRandom rand = new FastRandom();

                String nextAxion = generateNextAxion(rand, lSystemTree.axion);
                TreeStructure newTreeStructure = generateTreeFromAxion(treeLocation, nextAxion, lSystemTree.branchAngle, lSystemTree.rotationAngle);
                if (isWholeTreeSpaceLoaded(worldProvider, newTreeStructure)) {
                    lSystemTree.axion = nextAxion;
                    lSystemTree.generation++;
                    lSystemTree.lastGrowthTime = time;

                    if (!updateTreeInGame(worldProvider, oldTreeStructure.gatherBlockDefinitions(), newTreeStructure.gatherBlockDefinitions())) {
                        return FAILED_GROWTH_INTERVAL;
                    }

                    return checkForDeathAndSetupComponents(blockEntityRegistry, rand, treeLocation, lSystemTree);
                }
            }
            return (long) growthInterval;
        }
    }

    private Long checkForDeathAndSetupComponents(BlockEntityRegistry blockEntityRegistry, Random random, Vector3i location, LSystemTreeComponent treeComponent) {
        EntityRef entity = blockEntityRegistry.getBlockEntityAt(location);
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

        if (!checkForDeath(treeComponent.generation, random.nextFloat())) {
            return (long) growthInterval;
        } else {
            return null;
        }
    }

    private boolean isWholeTreeSpaceLoaded(WorldProvider worldProvider, TreeStructure treeStructure) {
        return worldProvider.isRegionRelevant(treeStructure.getTreeRegion());
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
                neighborLocation.add(connectSide.direction());

                if (treeBlocks.contains(neighborLocation)) {
                    connections += SideBitFlag.getSide(connectSide);
                }
            }
            return blockManager.getBlock(new BlockUri(blockFamilyUri, new Name(String.valueOf(connections))));
        } else {
            return blockManager.getBlock(blockFamilyUri);
        }
    }

    private boolean updateTreeInGame(WorldProvider worldProvider,
                                     Map<Vector3i, TreeBlockDefinition> currentTree, Map<Vector3i, TreeBlockDefinition> nextTree) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block air = blockManager.getBlock(BlockManager.AIR_ID);

        int replaceCount = 0;
        Map<Vector3i, Block> blocksToReplaceExistingTreeBlocks = new HashMap<>();
        Map<org.joml.Vector3i, Block> blocksToPlaceInNewPlaces = new HashMap<>();

        EntityRef worldEntity = worldProvider.getWorldEntity();

        for (Map.Entry<Vector3i, TreeBlockDefinition> newTreeBlock : nextTree.entrySet()) {
            Vector3i location = newTreeBlock.getKey();
            TreeBlockDefinition oldBlock = currentTree.remove(location);
            TreeBlockDefinition newBlock = newTreeBlock.getValue();

            Block resultBlock = getBlock(blockManager, newBlock, location, nextTree.keySet());

            if (oldBlock != null && !oldBlock.getBlockUri().equals(newBlock.getBlockUri())) {
                blocksToReplaceExistingTreeBlocks.put(location, resultBlock);
                replaceCount++;
            } else if (oldBlock == null) {
                if (worldProvider.getBlock(location).isReplacementAllowed()) {
                    blocksToPlaceInNewPlaces.put(location, resultBlock);
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
                    blocksToReplaceExistingTreeBlocks.put(location, air);
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
                result.append(axionElementReplacement.getReplacement(rand, axion.parameter, currentAxion));
            } else {
                result.append(axion.key);
                if (axion.parameter != null) {
                    result.append("(").append(axion.parameter).append(")");
                }
            }
        }

        return result.toString();
    }

    private TreeStructure generateTreeFromAxion(Vector3i location, String currentAxion, float angle, float treeRotation) {
        TreeStructure treeStructure = new TreeStructure();

        Deque<Vector3f> stackPosition = Queues.newArrayDeque();
        Deque<Matrix4f> stackOrientation = Queues.newArrayDeque();
        Deque<BranchLocation> stackBranch = Queues.newArrayDeque();

        BranchLocation branchLocation = treeStructure.getRootBranch();
        Vector3f position = new Vector3f(location);
        //TODO: use Quaternionf directly, as in https://github.com/Terasology/CoreWorlds/commit/6484ebc671bf27511a7eebceeb67e19968d7f33c
        Matrix4f rotation = new Matrix4f().translationRotateScale(new Vector3f(),new Quaternionf().setAngleAxis(treeRotation,0,1,0), 1.0f);

        Callback callback = new Callback(position, rotation);
        callback.setBranchLocation(branchLocation);

        int axionIndex = 0;
        for (AxionElement axion : parseAxions(currentAxion)) {
            Matrix4f tempRotation = new Matrix4f();
            tempRotation.identity();

            char c = axion.key;
            float currentAngle = axion.parameter == null ? angle : (float) Math.toRadians(Integer.parseInt(axion.parameter));
            switch (c) {
                case '[':
                    stackOrientation.push(new Matrix4f(rotation));
                    stackPosition.push(new Vector3f(position));
                    stackBranch.push(branchLocation);

                    branchLocation = branchLocation.addBranch(axionIndex);
                    callback.setBranchLocation(branchLocation);
                    break;
                case ']':
                    rotation.set(stackOrientation.pop());
                    position.set(stackPosition.pop());
                    branchLocation = stackBranch.pop();
                    callback.setBranchLocation(branchLocation);
                    break;
                case '&':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, 1, 0, 0));
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, -1, 0, 0));
                    rotation.mul(tempRotation);
                    break;
                case '+':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, 0, 1, 0));
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, 0, -1, 0));
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, 0, 0, 1));
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation = new Matrix4f().rotation(new Quaternionf().setAngleAxis(currentAngle, 0, 0, -1));
                    rotation.mul(tempRotation);
                    break;
                default:
                    AxionElementGeneration axionElementGeneration = blockMap.get(c);
                    if (axionElementGeneration != null) {
                        callback.setAxionIndex(axionIndex);
                        axionElementGeneration.generate(callback, position, rotation, axion.parameter);
                        axionIndex++;
                    }
            }
        }
        return treeStructure;
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

    public boolean isBlockOwnedByPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef treeRef) {
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);
        if (lSystemTree == null) {
            return false;
        }

        Vector3i location = treeRef.getComponent(BlockComponent.class).getPosition(new Vector3i());

        Map<Vector3i, TreeBlockDefinition> treeBlockMap = generateTreeFromAxion(location, lSystemTree.axion, lSystemTree.branchAngle, lSystemTree.rotationAngle)
                .gatherBlockDefinitions();
        return treeBlockMap.containsKey(new Vector3i(block.x - location.x, block.y - location.y, block.z - location.z));
    }

    public Collection<Vector3i> getBlocksConnectedTo(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef treeRef) {
        // Is it a tree at all
        LSystemTreeComponent lSystemTree = treeRef.getComponent(LSystemTreeComponent.class);
        if (lSystemTree == null) {
            return null;
        }

        Vector3i treeRootLocation = treeRef.getComponent(BlockComponent.class).getPosition(new Vector3i());

        // Does this tree have a block defined at that coordinate
        TreeStructure treeStructure = generateTreeFromAxion(treeRootLocation, lSystemTree.axion, lSystemTree.branchAngle, lSystemTree.rotationAngle);

        Map<Vector3i, TreeBlockDefinition> treeBlockMap = treeStructure.gatherBlockDefinitions();
        TreeBlockDefinition expectedBlockDefinition = treeBlockMap.get(block);
        if (expectedBlockDefinition == null) {
            return null;
        }

        // Is this block in world of the type it is supposed to be
        Block blockInWorld = worldProvider.getBlock(block);
        BlockUri familyUri = blockInWorld.getBlockFamily().getURI();
        BlockUri expectedFamilyUri = new BlockUri(expectedBlockDefinition.getBlockUri()).getFamilyUri();
        if (!familyUri.equals(expectedFamilyUri)) {
            return null;
        }

        return treeStructure.getBlocksConnectedTo(worldProvider, block);
    }

    private final class TreeStructure {
        private BranchLocation rootBranch = new BranchLocation(0);
        private Map<Vector3i, TreeBlockDefinition> blockDefinitionsCache;

        public BranchLocation getRootBranch() {
            return rootBranch;
        }

        public Map<Vector3i, TreeBlockDefinition> gatherBlockDefinitions() {
            if (blockDefinitionsCache != null) {
                return blockDefinitionsCache;
            }
            Map<Vector3i, TreeBlockDefinition> result = new LinkedHashMap<>();
            rootBranch.fillBlockDefinitions(result);
            blockDefinitionsCache = result;
            return result;
        }

        public Collection<Vector3i> getBlocksConnectedTo(WorldProvider worldProvider, Vector3i block) {
            PositionOfBlock pob = rootBranch.getPositionOfBlock(block);
            if (pob == null) {
                return Collections.emptySet();
            }
            Map<Vector3i, TreeBlockDefinition> blockDefinitionsBeforeTrim = gatherBlockDefinitions();

            Map<Vector3i, TreeBlockDefinition> connected = new LinkedHashMap<>(blockDefinitionsBeforeTrim);
            Iterator<Map.Entry<Vector3i, TreeBlockDefinition>> blockIterator = connected.entrySet().iterator();
            while (blockIterator.hasNext()) {
                Map.Entry<Vector3i, TreeBlockDefinition> blockAtLocation = blockIterator.next();

                Block blockInWorld = worldProvider.getBlock(blockAtLocation.getKey());
                BlockUri familyUri = blockInWorld.getBlockFamily().getURI();
                BlockUri expectedFamilyUri = new BlockUri(blockAtLocation.getValue().getBlockUri()).getFamilyUri();
                if (!familyUri.equals(expectedFamilyUri)) {
                    blockIterator.remove();
                }
            }

            pob.branchLocation.trimEverythingAfter(pob.axionIndex);
            blockDefinitionsCache = null;
            Map<Vector3i, TreeBlockDefinition> blockDefinitionsAfterTrim = gatherBlockDefinitions();

            for (Vector3i location : blockDefinitionsAfterTrim.keySet()) {
                connected.remove(location);
            }

            return connected.keySet();
        }

        public BlockRegion getTreeRegion() {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;

            for (Vector3i block : gatherBlockDefinitions().keySet()) {
                minX = Math.min(minX, block.x);
                minY = Math.min(minY, block.y);
                minZ = Math.min(minZ, block.z);
                maxX = Math.max(maxX, block.x);
                maxY = Math.max(maxY, block.y);
                maxZ = Math.max(maxZ, block.z);
            }
            return new BlockRegion(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private final class BranchLocation {
        private int startIndex;
        private Map<Vector3i, Integer> mainBlocks = new LinkedHashMap<>();
        private Map<Integer, Map<Vector3i, TreeBlockDefinition>> indexBlocks = new LinkedHashMap<>();

        private List<BranchLocation> branches = new LinkedList<>();

        private BranchLocation(int startIndex) {
            this.startIndex = startIndex;
        }

        public BranchLocation addBranch(int branchStartIndex) {
            BranchLocation result = new BranchLocation(branchStartIndex);
            branches.add(result);
            return result;
        }

        public void setMainBlock(int index, Vector3i vector) {
            if (mainBlocks.get(vector) == null) {
                mainBlocks.put(vector, index);
            }
        }

        public void addTreeBlock(int index, Vector3i vector, TreeBlockDefinition blockDefinition) {
            Map<Vector3i, TreeBlockDefinition> blockAtPositions = indexBlocks.get(index);
            if (blockAtPositions == null) {
                blockAtPositions = new LinkedHashMap<>();
                indexBlocks.put(index, blockAtPositions);
            }
            putDefinitionIfMoreImportant(vector, blockDefinition, blockAtPositions);
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void trimEverythingAfter(int index) {
            Iterator<Map.Entry<Integer, Map<Vector3i, TreeBlockDefinition>>> axiomElements = indexBlocks.entrySet().iterator();
            while (axiomElements.hasNext()) {
                Map.Entry<Integer, Map<Vector3i, TreeBlockDefinition>> axiomElement = axiomElements.next();
                if (axiomElement.getKey() > index) {
                    axiomElements.remove();
                }
            }

            Iterator<BranchLocation> branchesIterator = branches.iterator();
            while (branchesIterator.hasNext()) {
                BranchLocation branch = branchesIterator.next();
                if (branch.getStartIndex() > index) {
                    branchesIterator.remove();
                }
            }
        }

        private void putDefinitionIfMoreImportant(Vector3i vector, TreeBlockDefinition blockDefinition, Map<Vector3i, TreeBlockDefinition> blockAtPositions) {
            TreeBlockDefinition oldDefinition = blockAtPositions.get(vector);
            if (oldDefinition == null || oldDefinition.getTreePart().getPriority() < blockDefinition.getTreePart().getPriority()) {
                blockAtPositions.put(vector, blockDefinition);
            }
        }

        public void fillBlockDefinitions(Map<Vector3i, TreeBlockDefinition> result) {
            for (Map<Vector3i, TreeBlockDefinition> blockDefinitions : indexBlocks.values()) {
                for (Map.Entry<Vector3i, TreeBlockDefinition> blockDefinition : blockDefinitions.entrySet()) {
                    putDefinitionIfMoreImportant(blockDefinition.getKey(), blockDefinition.getValue(), result);
                }
            }

            for (BranchLocation branch : branches) {
                branch.fillBlockDefinitions(result);
            }
        }

        public PositionOfBlock getPositionOfBlock(Vector3i block) {
            Integer result = mainBlocks.get(block);
            if (result != null) {
                return new PositionOfBlock(result, this);
            }

            for (BranchLocation branch : branches) {
                PositionOfBlock pob = branch.getPositionOfBlock(block);
                if (pob != null) {
                    return pob;
                }
            }
            return null;
        }
    }

    private final class PositionOfBlock {
        private BranchLocation branchLocation;
        private int axionIndex;

        private PositionOfBlock(int axionIndex, BranchLocation branchLocation) {
            this.axionIndex = axionIndex;
            this.branchLocation = branchLocation;
        }
    }

    private final class Callback implements AxionElementGeneration.AxionElementGenerationCallback {
        private BranchLocation branchLocation;
        private int axionIndex;
        private Vector3f position;
        private Matrix4f rotation;

        private Callback(Vector3f position, Matrix4f rotation) {
            this.position = position;
            this.rotation = rotation;
        }

        public void setBranchLocation(BranchLocation branchLocation) {
            this.branchLocation = branchLocation;
        }

        public void setAxionIndex(int axionIndex) {
            this.axionIndex = axionIndex;
        }

        private void setPosition(Vector3f position) {
            this.position = position;
        }

        private void setRotation(Matrix4f rotation) {
            this.rotation = rotation;
        }

        @Override
        public void setMainBlock(Vector3fc blockPosition, TreeBlockDefinition blockDefinition) {
            Vector3i integerPosition = new Vector3i(new Vector3f(blockPosition.x() + 0.5f, blockPosition.y() + 0.5f, blockPosition.z() + 0.5f), RoundingMode.FLOOR);
            if (integerPosition.y >= 0) {
                branchLocation.setMainBlock(axionIndex, integerPosition);
                branchLocation.addTreeBlock(axionIndex, integerPosition, blockDefinition);
            }
        }

        @Override
        public void setAdditionalBlock(Vector3fc blockPosition, TreeBlockDefinition blockDefinition) {
            Vector3i integerPosition = new Vector3i(new Vector3f(blockPosition.x() + 0.5f, blockPosition.y() + 0.5f, blockPosition.z() + 0.5f), RoundingMode.FLOOR);
            if (integerPosition.y >= 0) {
                branchLocation.addTreeBlock(axionIndex, integerPosition, blockDefinition);
            }
        }

        @Override
        public void advance(float distance) {
            Vector3f dir = new Vector3f(0, distance, 0);
            rotation.transformDirection(dir);
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
