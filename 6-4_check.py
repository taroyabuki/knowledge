import networkx as nx
import pandas as pd

# CSVファイルを読み込む
df = pd.read_csv('/var/lib/neo4j/import/nr-station-links.csv')

# グラフを作成
G = nx.Graph()

# エッジを追加（距離を重みとして使用）
for _, row in df.iterrows():
    G.add_edge(row['from'], row['to'], weight=row['distance'])

try:
    # BHMからEDBまでの最短距離を計算
    shortest_distance = nx.shortest_path_length(G, 'BHM', 'EDB', weight='weight')
    path = nx.shortest_path(G, 'BHM', 'EDB', weight='weight')
    
    print(f"最短距離: {shortest_distance:.2f}")
    print("経路:", ' -> '.join(path))

except nx.NetworkXNoPath:
    print("経路が見つかりません")
except nx.NodeNotFound:
    print("指定された駅が見つかりません")
