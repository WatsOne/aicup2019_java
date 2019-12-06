package path;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import model.Level;

public class PathFinder {
    private byte[][] mGrid;
    private Level mMap;

    private ArrayList<PathFinderNodeFast>[] nodes;
    private Stack<Integer> touchedLocations;
    PriorityQueue<Location> mOpen;

    private short mGridX;
    private short mGridY;
    private ArrayList<Vector2i> mClose;

    private boolean mFound;
    private boolean mStop;
    private boolean mStopped;
    private int mCloseNodeCounter;
    private byte mOpenNodeValue = 1;
    private byte mCloseNodeValue = 2;

    private Location mLocation;
    private int mNewLocation = 0;
    private int mEndLocation = 0;
    private short mLocationX;
    private short mLocationY;
    private short mNewLocationX;
    private short mNewLocationY;
    private int mSearchLimit = 2000;
    private byte[][] mDirection = new byte[][] {{0,-1} , {1,0}, {0,1}, {-1,0}, {1,-1}, {1,1}, {-1,1}, {-1,-1}};
    private int mNewG;
    private int mH;
    private int mHEstimate = 2;

    public PathFinder(byte[][] grid, Level mMap) {
        mGrid = grid;
        this.mMap = mMap;

        mGridX = (short) grid.length;
        mGridY = (short) grid[0].length;

        nodes = new ArrayList[mGridX * mGridY];
        touchedLocations = new Stack<>();
        mClose = new ArrayList<>(mGridX * mGridY);

        for (int i = 0; i < nodes.length; ++i)
            nodes[i] = new ArrayList<>(1);

        mOpen = new PriorityQueue<>(new ComparePFNodeMatrix(nodes));

    }

    public List<Vector2i> find(Vector2i start, Vector2i end, int characterWidth, int characterHeight, short maxCharacterJumpHeight) {
        while (touchedLocations.size() > 0)
            nodes[touchedLocations.pop()].clear();

        if (mGrid[end.getX()][end.getY()] == 0)
            return null;

        mFound = false;
        mStop = false;
        mStopped = false;
        mCloseNodeCounter = 0;
        mOpenNodeValue += 2;
        mCloseNodeValue += 2;
        mOpen.clear();

        mLocation = new Location((start.getY() * mGridX) + start.getX(), 0);
        mEndLocation = (end.getY() * mGridX) + end.getX();

        PathFinderNodeFast firstNode = new PathFinderNodeFast();
        firstNode.setG(0);
        firstNode.setF(mHEstimate);
        firstNode.setPx((short) start.getX());
        firstNode.setPy((short) start.getY());
        firstNode.setPz((byte) 0);
        firstNode.setStatus(mOpenNodeValue);

        if (mMap.isGround(start.getX(), start.getY() - 1))
            firstNode.setJumpLength((short) 0);
        else
            firstNode.setJumpLength((short)(maxCharacterJumpHeight * 2));

        nodes[mLocation.getXy()].add(firstNode);
        touchedLocations.push(mLocation.getXy());

        mOpen.add(mLocation);

        while(mOpen.size() > 0 && !mStop) {
            mLocation = mOpen.poll();
            if (nodes[mLocation.getXy()].get(mLocation.getZ()).getStatus() == mCloseNodeValue)
                continue;

            mLocationX = (short) (mLocation.getXy() % mGridX);
            mLocationY = (short) (mLocation.getXy() / mGridX);

            if (mLocation.getXy() == mEndLocation) {
                nodes[mLocation.getXy()].get(mLocation.getZ()).setStatus(mCloseNodeValue);
                mFound = true;
                break;
            }

            if (mCloseNodeCounter > mSearchLimit) {
                mStopped = true;
                return null;
            }

            for (int i = 0; i < 8; i++) {
                mNewLocationX = (short) (mLocationX + mDirection[i][0]);
                mNewLocationY = (short) (mLocationY + mDirection[i][1]);

                mNewLocation  = (mNewLocationY * mGridX) + mNewLocationX;

                boolean onGround = false;
                boolean atCeiling = false;

                if (mGrid[mNewLocationX][mNewLocationY] == 0)
                    continue;

                if (mMap.isGround(mNewLocationX, mNewLocationY - 1))
                    onGround = true;
                else if (mGrid[mNewLocationX][mNewLocationY + characterHeight] == 0)
                    atCeiling = true;

                short jumpLength = nodes[mLocation.getXy()].get(mLocation.getZ()).getJumpLength();
                short newJumpLength = jumpLength;

                if (atCeiling) {
                    if (mNewLocationX != mLocationX)
                        newJumpLength = (short) Math.max(maxCharacterJumpHeight * 2 + 1, jumpLength + 1);
                    else
                        newJumpLength = (short) Math.max(maxCharacterJumpHeight * 2, jumpLength + 2);
                } else if (onGround)
                    newJumpLength = 0;
                else if (mNewLocationY > mLocationY) {
                    if (jumpLength < 2) //first jump is always two block up instead of one up and optionally one to either right or left
                        newJumpLength = 3;
                    else  if (jumpLength % 2 == 0)
                        newJumpLength = (short)(jumpLength + 2);
                    else
                        newJumpLength = (short)(jumpLength + 1);
                } else if (mNewLocationY < mLocationY) {
                    if (jumpLength % 2 == 0)
                        newJumpLength = (short) Math.max(maxCharacterJumpHeight * 2, jumpLength + 2);
                    else
                        newJumpLength = (short) Math.max(maxCharacterJumpHeight * 2, jumpLength + 1);
                } else if (mNewLocationX != mLocationX)
                    newJumpLength = (short)(jumpLength + 1);

                if (jumpLength >= 0 && jumpLength % 2 != 0 && mLocationX != mNewLocationX)
                    continue;

                if (jumpLength >= maxCharacterJumpHeight * 2 && mNewLocationY > mLocationY)
                    continue;

                if (newJumpLength >= maxCharacterJumpHeight * 2 + 6 && mNewLocationX != mLocationX && (newJumpLength - (maxCharacterJumpHeight * 2 + 6)) % 8 != 3)
                    continue;

                mNewG = nodes[mLocation.getXy()].get(mLocation.getZ()).getG() + mGrid[mNewLocationX][mNewLocationY] + newJumpLength / 4;

                if (nodes[mNewLocation].size() > 0) {
                    int lowestJump = Short.MAX_VALUE;
                    boolean couldMoveSideways = false;
                    for (int j = 0; j < nodes[mNewLocation].size(); ++j) {
                        if (nodes[mNewLocation].get(j).getJumpLength() < lowestJump)
                            lowestJump = nodes[mNewLocation].get(j).getJumpLength();

                        if (nodes[mNewLocation].get(j).getJumpLength() % 2 == 0 && nodes[mNewLocation].get(j).getJumpLength() < maxCharacterJumpHeight * 2 + 6)
                            couldMoveSideways = true;
                    }

                    if (lowestJump <= newJumpLength && (newJumpLength % 2 != 0 || newJumpLength >= maxCharacterJumpHeight * 2 + 6 || couldMoveSideways))
                        continue;
                }

                mH = mHEstimate * (Math.abs(mNewLocationX - end.getX()) + Math.abs(mNewLocationY - end.getY()));

                PathFinderNodeFast newNode = new PathFinderNodeFast();
                newNode.setJumpLength(newJumpLength);
                newNode.setPx(mLocationX);
                newNode.setPy(mLocationY);
                newNode.setPz((byte) mLocation.getZ());
                newNode.setG(mNewG);
                newNode.setF(mNewG + mH);
                newNode.setStatus(mOpenNodeValue);

                if (nodes[mNewLocation].size() == 0)
                    touchedLocations.push(mNewLocation);

                nodes[mNewLocation].add(newNode);
                mOpen.add(new Location(mNewLocation, nodes[mNewLocation].size() - 1));
            }

            nodes[mLocation.getXy()].get(mLocation.getZ()).setStatus(mCloseNodeValue);
            mCloseNodeCounter++;
        }

        if (mFound) {
            mClose.clear();
            int posX = end.getX();
            int posY = end.getY();

            PathFinderNodeFast fPrevNodeTmp = new PathFinderNodeFast();
            PathFinderNodeFast fNodeTmp = nodes[mEndLocation].get(0);

            Vector2i fNode = end;
            Vector2i fPrevNode = end;

            int loc = (fNodeTmp.getPy() * mGridX) + fNodeTmp.getPx();

            while (fNode.getX() != fNodeTmp.getPx() || fNode.getY() != fNodeTmp.getPy()) {
                PathFinderNodeFast fNextNodeTmp = nodes[loc].get(fNodeTmp.getPz());

                if ((mClose.size() == 0)
                    || (fNextNodeTmp.getJumpLength() != 0 && fNodeTmp.getJumpLength() == 0)                                                                                                       //mark jumps starts
                    || (fNodeTmp.getJumpLength() == 0 && fPrevNodeTmp.getJumpLength() != 0)                                                                                                       //mark landings
                    || (fNode.getY() > mClose.get(mClose.size() - 1).getY() && fNode.getY() > fNodeTmp.getPy())
                    || (fNode.getY() < mClose.get(mClose.size() - 1).getY() && fNode.getY() < fNodeTmp.getPy())
                    || ((mMap.isGround(fNode.getX() - 1, fNode.getY()) || mMap.isGround(fNode.getX() + 1, fNode.getY()))
                    && fNode.getY() != mClose.get(mClose.size() - 1).getY() && fNode.getX() != mClose.get(mClose.size() - 1).getX()))
                mClose.add(fNode);

                fPrevNode = fNode;
                posX = fNodeTmp.getPx();
                posY = fNodeTmp.getPy();
                fPrevNodeTmp = fNodeTmp;
                fNodeTmp = fNextNodeTmp;
                loc = (fNodeTmp.getPy() * mGridX) + fNodeTmp.getPx();
                fNode = new Vector2i(posX, posY);
            }

            mClose.add(fNode);

            mStopped = true;

            return mClose;
        }

        mStopped = true;
        return null;
    }
}
