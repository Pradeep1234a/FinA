def get_ans(N, P, A):
    from collections import defaultdict, deque
    
    # Build the tree using the parent array
    tree = defaultdict(list)
    for child in range(1, N):
        parent = P[child]
        tree[parent].append(child)
        tree[child].append(parent)
    
    # Function to check if the path between two nodes is good
    def is_good_path(u, v):
        return (A[u] ^ A[v]) < min(A[u], A[v])
    
    # Function to perform DFS and find the longest good path
    def dfs(node, parent):
        max_length = 1
        for neighbor in tree[node]:
            if neighbor != parent:
                length = dfs(neighbor, node)
                if is_good_path(node, neighbor):
                    max_length = max(max_length, 1 + length)
        return max_length
    
    # Perform DFS from the root node (node 0)
    return dfs(0, -1)

def main():
    import sys
    input = sys.stdin.read
    data = input().strip().split()
    
    index = 0
    N = int(data[index])
    index += 1
    
    P = []
    for _ in range(N):
        P.append(int(data[index]))
        index += 1
    
    A = []
    for _ in range(N):
        A.append(int(data[index]))
        index += 1
    
    result = get_ans(N, P, A)
    print(result)

if __name__ == "__main__": 
    main()