import { useCallback, useEffect, useMemo, useRef, useState } from "react";

const ITEMS_ENDPOINT = "/item/get-all-items";
const ACTIVE_ORDERS_ENDPOINT = "/orders/get-active-orders";
const CREATE_ORDER_ENDPOINT = "/orders/create-order";
const CREATE_CHEF_ENDPOINT = "/chefs/insert-chef";
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

async function request(path, options) {
  const response = await fetch(`${API_BASE_URL}${path}`, options);
  const body = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(body.msg || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }

  return body;
}

function getReadyAt(time, now) {
  if (typeof time !== "string") return null;

  const match = time.match(/^(\d{2}):(\d{2})(?::(\d{2}))?/);
  if (!match) return null;

  const readyAt = new Date(now);
  readyAt.setHours(Number(match[1]), Number(match[2]), Number(match[3] || 0), 0);

  // The API returns a time without a date; allow a ready time just after midnight.
  if (readyAt.getTime() < now.getTime() - 12 * 60 * 60 * 1000) {
    readyAt.setDate(readyAt.getDate() + 1);
  }

  return readyAt;
}

function formatCountdown(milliseconds) {
  const totalSeconds = Math.max(0, Math.ceil(milliseconds / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function formatCurrency(value) {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(value);
}

function App() {
  const [items, setItems] = useState([]);
  const [selectedItemIds, setSelectedItemIds] = useState([]);
  const [activeOrders, setActiveOrders] = useState([]);
  const [itemsLoading, setItemsLoading] = useState(true);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [addingChef, setAddingChef] = useState(false);
  const [chefName, setChefName] = useState("");
  const [itemsError, setItemsError] = useState("");
  const [ordersError, setOrdersError] = useState("");
  const [notice, setNotice] = useState("");
  const [chefNotice, setChefNotice] = useState("");
  const [chefError, setChefError] = useState("");
  const [now, setNow] = useState(() => new Date());
  const nextOrderId = useRef(1);

  const loadItems = useCallback(async () => {
    setItemsLoading(true);
    setItemsError("");
    try {
      const body = await request(ITEMS_ENDPOINT);
      setItems(Array.isArray(body.items) ? body.items : []);
    } catch (error) {
      setItemsError(error.message);
    } finally {
      setItemsLoading(false);
    }
  }, []);

  const loadActiveOrders = useCallback(async () => {
    setOrdersError("");
    try {
      const body = await request(ACTIVE_ORDERS_ENDPOINT);
      setActiveOrders(Array.isArray(body.allOrders) ? body.allOrders : []);
    } catch (error) {
      if (error.status === 404) {
        setActiveOrders([]);
      } else {
        setOrdersError(error.message);
      }
    } finally {
      setOrdersLoading(false);
    }
  }, []);

  useEffect(() => {
    loadItems();
    loadActiveOrders();

    const refreshInterval = window.setInterval(loadActiveOrders, 15000);
    const clockInterval = window.setInterval(() => setNow(new Date()), 1000);
    return () => {
      window.clearInterval(refreshInterval);
      window.clearInterval(clockInterval);
    };
  }, [loadActiveOrders, loadItems]);

  const itemById = useMemo(
    () => new Map(items.map((item) => [String(item.itemId), item])),
    [items],
  );

  const selectedItems = selectedItemIds
    .map((id) => itemById.get(String(id)))
    .filter(Boolean);
  const orderTotal = selectedItems.reduce((total, item) => total + item.itemValue, 0);

  function toggleItem(itemId) {
    setNotice("");
    setSelectedItemIds((current) =>
      current.includes(itemId)
        ? current.filter((id) => id !== itemId)
        : [...current, itemId],
    );
  }

  async function submitOrder(event) {
    event.preventDefault();
    if (selectedItemIds.length === 0 || submitting) return;

    setSubmitting(true);
    setNotice("");
    try {
      const orderId = nextOrderId.current++;
      const body = await request(CREATE_ORDER_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ orderId, itemIds: selectedItemIds }),
      });

      setNotice(body.msg || `Order #${orderId} placed successfully.`);
      setSelectedItemIds([]);
      await loadActiveOrders();
    } catch (error) {
      setNotice(
        `${error.message}. Check that the backend is running and at least one chef is registered.`,
      );
    } finally {
      setSubmitting(false);
    }
  }

  async function submitChef(event) {
    event.preventDefault();
    const name = chefName.trim();
    if (!name || addingChef) return;

    setAddingChef(true);
    setChefNotice("");
    setChefError("");
    try {
      const body = await request(CREATE_CHEF_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ chefName: name }),
      });

      setChefName("");
      setChefNotice(body.msg || `${name} was added to the kitchen.`);
    } catch (error) {
      setChefError(error.message);
    } finally {
      setAddingChef(false);
    }
  }

  const displayedOrders = activeOrders
    .map((order) => {
      const readyAt = getReadyAt(order.timeWhenOrderReady, now);
      const item = itemById.get(String(order.itemId));
      return { ...order, readyAt, item };
    })
    .filter((order) => order.readyAt && order.readyAt.getTime() > now.getTime())
    .sort((a, b) => a.readyAt - b.readyAt);

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="#" aria-label="Moonwalk Kitchen home">
          <span className="brand-mark" aria-hidden="true">M</span>
          <span>moonwalk<span className="brand-period">.</span></span>
        </a>
        <span className="service-status"><span /> Kitchen is open</span>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">MADE FRESH, JUST FOR YOU</p>
          <h1>A little joy<br />is on the menu.</h1>
          <p className="hero-description">
            Pick your favourites, place your order, and we’ll keep you posted while our kitchen gets to work.
          </p>
          <a className="text-link" href="#menu">Explore the menu <span aria-hidden="true">↓</span></a>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="sun-shape" />
          <div className="plate">
            <div className="plate-center">
              <span className="food-leaf leaf-one" />
              <span className="food-leaf leaf-two" />
              <span className="food-leaf leaf-three" />
              <span className="food-dot dot-one" />
              <span className="food-dot dot-two" />
              <span className="food-dot dot-three" />
              <span className="food-dot dot-four" />
            </div>
          </div>
          <span className="sparkle sparkle-one">✳</span>
          <span className="sparkle sparkle-two">✳</span>
        </div>
      </section>

      <div className="content-grid">
        <section className="menu-section" id="menu">
          <div className="section-heading">
            <div>
              <p className="eyebrow">THE GOOD STUFF</p>
              <h2>Today’s menu</h2>
            </div>
            <span className="item-count">{items.length} {items.length === 1 ? "item" : "items"}</span>
          </div>

          {itemsError && (
            <div className="inline-error" role="alert">
              <span>{itemsError}</span>
              <button className="retry-button" onClick={loadItems}>Try again</button>
            </div>
          )}

          {itemsLoading ? (
            <div className="empty-state">Loading the menu…</div>
          ) : items.length === 0 && !itemsError ? (
            <div className="empty-state">Nothing on the menu just yet. Check back soon.</div>
          ) : (
            <form onSubmit={submitOrder}>
              <div className="menu-list">
                {items.map((item, index) => {
                  const selected = selectedItemIds.includes(item.itemId);
                  return (
                    <label className={`menu-card${selected ? " is-selected" : ""}`} key={item.itemId}>
                      <input
                        type="checkbox"
                        checked={selected}
                        onChange={() => toggleItem(item.itemId)}
                        aria-label={`Add ${item.itemName} to your order`}
                      />
                      <span className={`dish-art dish-art-${index % 4}`} aria-hidden="true">
                        <span>{["✳", "●", "✿", "◒"][index % 4]}</span>
                      </span>
                      <span className="dish-info">
                        <span className="dish-name">{item.itemName}</span>
                        <span className="prep-time">
                          <span aria-hidden="true">◷</span> {item.preparationTime} min prep
                        </span>
                      </span>
                      <span className="dish-price">{formatCurrency(item.itemValue)}</span>
                      <span className="add-indicator" aria-hidden="true">{selected ? "✓" : "+"}</span>
                    </label>
                  );
                })}
              </div>
              <div className="order-bar">
                <div className="order-summary">
                  <span>{selectedItems.length} {selectedItems.length === 1 ? "item" : "items"} selected</span>
                  <strong>{formatCurrency(orderTotal)}</strong>
                </div>
                <button className="primary-button" type="submit" disabled={!selectedItems.length || submitting}>
                  {submitting ? "Placing order…" : "Place order"}
                  {!submitting && <span aria-hidden="true">↗</span>}
                </button>
              </div>
              {notice && <p className="form-notice" role="status">{notice}</p>}
              <p className="form-hint">Choose each menu item once per order.</p>
            </form>
          )}
        </section>

        <aside className="orders-panel" aria-labelledby="orders-title">
          <div className="orders-heading">
            <div>
              <p className="eyebrow">THE KITCHEN’S ON IT</p>
              <h2 id="orders-title">On the way</h2>
            </div>
            <span className="live-indicator"><span /> LIVE</span>
          </div>

          {ordersError && (
            <div className="inline-error" role="alert">
              <span>{ordersError}</span>
              <button className="retry-button" onClick={loadActiveOrders}>Retry</button>
            </div>
          )}

          {ordersLoading ? (
            <div className="empty-state">Checking with the kitchen…</div>
          ) : displayedOrders.length === 0 ? (
            <div className="orders-empty">
              <span className="empty-illustration" aria-hidden="true">☼</span>
              <strong>All caught up.</strong>
              <span>Your active orders will show up here.</span>
            </div>
          ) : (
            <div className="active-order-list">
              {displayedOrders.map((order) => {
                const remaining = order.readyAt.getTime() - now.getTime();
                return (
                  <article className="active-order" key={`${order.orderId}-${order.itemId}`}>
                    <div className="active-order-top">
                      <span className="order-label">ORDER #{order.orderId}</span>
                      <span className="preparing-label"><span /> PREPARING</span>
                    </div>
                    <strong className="active-item-name">{order.item?.itemName || `Menu item ${order.itemId}`}</strong>
                    <div className="countdown-row">
                      <span>Ready in</span>
                      <strong className="countdown">{formatCountdown(remaining)}</strong>
                    </div>
                    <div
                      className="countdown-track"
                      role="progressbar"
                      aria-label={`Time remaining for order ${order.orderId}`}
                      aria-valuemin="0"
                      aria-valuemax={Math.max(order.item?.preparationTime * 60 || 1, 1)}
                      aria-valuenow={Math.min(
                        Math.max(0, Math.ceil(remaining / 1000)),
                        Math.max(order.item?.preparationTime * 60 || 1, 1),
                      )}
                    >
                      <span
                        style={{
                          width: `${Math.min(
                            100,
                            Math.max(
                              0,
                              (remaining / Math.max(order.item?.preparationTime * 60 * 1000 || remaining, 1)) * 100,
                            ),
                          )}%`,
                        }}
                      />
                    </div>
                  </article>
                );
              })}
            </div>
          )}
          <p className="refresh-note">Order status refreshes automatically.</p>
          <div className="chef-section">
            <p className="eyebrow">GROW THE TEAM</p>
            <h3>Add a chef</h3>
            <form className="chef-form" onSubmit={submitChef}>
              <label className="visually-hidden" htmlFor="chef-name">Chef name</label>
              <input
                id="chef-name"
                name="chefName"
                type="text"
                autoComplete="name"
                maxLength={100}
                placeholder="Enter chef’s name"
                value={chefName}
                onChange={(event) => {
                  setChefName(event.target.value);
                  setChefError("");
                  setChefNotice("");
                }}
                required
              />
              <button className="chef-button" type="submit" disabled={!chefName.trim() || addingChef}>
                {addingChef ? "Adding…" : "Add chef"}
              </button>
            </form>
            {chefError && <p className="chef-message chef-message-error" role="alert">{chefError}</p>}
            {chefNotice && <p className="chef-message" role="status">{chefNotice}</p>}
          </div>
        </aside>
      </div>

      <footer className="footer">
        <span>Good food, good mood.</span>
        <span>Made with care at Moonwalk <span aria-hidden="true">✳</span></span>
      </footer>
    </main>
  );
}

export default App;
