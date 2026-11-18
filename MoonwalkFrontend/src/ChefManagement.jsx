import { useEffect, useState } from "react";
import { request } from "./api.js";

const CHEFS_ENDPOINT = "/chefs/get-all-chefs";
const CREATE_CHEF_ENDPOINT = "/chefs/insert-chef";

function ChefManagement() {
  const [chefs, setChefs] = useState([]);
  const [chefName, setChefName] = useState("");
  const [loading, setLoading] = useState(true);
  const [addingChef, setAddingChef] = useState(false);
  const [deletingChefId, setDeletingChefId] = useState(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  async function loadChefs() {
    setLoading(true);
    setError("");
    try {
      const body = await request(CHEFS_ENDPOINT);
      setChefs(Array.isArray(body.chefs) ? body.chefs : []);
    } catch (requestError) {
      if (requestError.status === 404) {
        setChefs([]);
      } else {
        setError(requestError.message);
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadChefs();
  }, []);

  async function submitChef(event) {
    event.preventDefault();
    const name = chefName.trim();
    if (!name || addingChef) return;

    setAddingChef(true);
    setError("");
    setNotice("");
    try {
      const body = await request(CREATE_CHEF_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ chefName: name }),
      });
      setChefName("");
      setNotice(body.msg || `${name} was added to the kitchen.`);
      await loadChefs();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setAddingChef(false);
    }
  }

  async function deleteChef(chef) {
    if (deletingChefId !== null) return;

    setDeletingChefId(chef.chefId);
    setError("");
    setNotice("");
    try {
      const body = await request(`/chefs/delete-chef/${chef.chefId}`, {
        method: "DELETE",
      });
      setNotice(body.msg || `${chef.chefName} was removed from the kitchen.`);
      await loadChefs();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setDeletingChefId(null);
    }
  }

  return (
    <main className="app-shell chef-page">
      <header className="topbar">
        <a className="brand" href="/" aria-label="Moonwalk Kitchen home">
          <span className="brand-mark" aria-hidden="true">M</span>
          <span>moonwalk<span className="brand-period">.</span></span>
        </a>
        <a className="chef-back-link" href="/">← Back to menu</a>
      </header>

      <section className="chef-hero">
        <p className="eyebrow">GROW THE TEAM</p>
        <h1>Meet the chefs.</h1>
        <p>Manage the people bringing your menu to life.</p>
      </section>

      <div className="chef-management-grid">
        <section className="chef-panel" aria-labelledby="chef-list-title">
          <div className="chef-panel-heading">
            <div>
              <p className="eyebrow">YOUR KITCHEN CREW</p>
              <h2 id="chef-list-title">Chef list</h2>
            </div>
            <span className="item-count">{chefs.length} {chefs.length === 1 ? "chef" : "chefs"}</span>
          </div>

          {error && <div className="inline-error" role="alert">
            <span>{error}</span>
            <button className="retry-button" type="button" onClick={loadChefs}>Try again</button>
          </div>}

          {loading ? (
            <div className="empty-state">Checking in with the kitchen…</div>
          ) : chefs.length === 0 && !error ? (
            <div className="empty-state">No chefs yet. Add the first member of your kitchen crew.</div>
          ) : (
            <ul className="chef-list">
              {chefs.map((chef, index) => (
                <li className="chef-card" key={chef.chefId}>
                  <span className="chef-avatar" aria-hidden="true">{chef.chefName?.charAt(0)?.toUpperCase() || "C"}</span>
                  <span className="chef-card-details">
                    <strong>{chef.chefName}</strong>
                    <span>Chef no. {index + 1}</span>
                  </span>
                  <button
                    className="chef-delete-button"
                    type="button"
                    onClick={() => deleteChef(chef)}
                    disabled={deletingChefId !== null}
                    aria-label={`Delete ${chef.chefName}`}
                  >
                    {deletingChefId === chef.chefId ? "Removing…" : "Delete"}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="chef-panel chef-add-panel" aria-labelledby="add-chef-title">
          <p className="eyebrow">A PLACE AT THE TABLE</p>
          <h2 id="add-chef-title">Add a chef</h2>
          <p className="chef-panel-description">Add a team member to help prepare upcoming orders.</p>
          <form className="chef-add-form" onSubmit={submitChef}>
            <label htmlFor="chef-name">Chef name</label>
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
                setError("");
                setNotice("");
              }}
              required
            />
            <button className="primary-button chef-submit-button" type="submit" disabled={!chefName.trim() || addingChef}>
              {addingChef ? "Adding chef…" : "Add chef"}
              {!addingChef && <span aria-hidden="true">↗</span>}
            </button>
          </form>
          {notice && <p className="chef-message" role="status">{notice}</p>}
        </section>
      </div>

      <footer className="footer">
        <span>Good food, good mood.</span>
        <span>Made with care at Moonwalk <span aria-hidden="true">✳</span></span>
      </footer>
    </main>
  );
}

export default ChefManagement;
